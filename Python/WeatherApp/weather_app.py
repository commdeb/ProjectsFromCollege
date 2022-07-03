import configparser
import datetime as dt
from datetime import datetime
import tkinter as tk
import tkinter.ttk
import tkinter.messagebox
from weather_info_container import WeatherReportFabric
from request_api_handler import RequestApiCreator
from tkcalendar import Calendar
from tktimepicker import SpinTimePickerModern
from tkinter import ttk
import time
import tkinter.messagebox
from tkinter.constants import NSEW
from PIL import ImageTk, Image
import os
import json

config_file_path = "tc.txt"


class WeatherApp(tk.Frame):
    link_temp = "https://api.open-meteo.com/v1/forecast?"
    requests_temp = {"latitude": None,
                     "longitude": None,
                     "hourly": [],
                     "daily": [],
                     "current_weather": None,
                     "temperature_unit": None,
                     "windspeed_unit": None,
                     "precipitation_unit": None,
                     "timeformat": None,
                     "timezone": None,
                     "past_days": None}

    theme_1 = None
    theme_2 = None
    theme_3 = None

    def __init__(self, master=None):
        self.favorite_loc = None
        self.label_names = None
        self.def_loc_name = None
        self.values_request = None
        self.numb_of_default_loc_name = 0
        self.sel_add_name = tk.StringVar()
        self.sel_opt_rm = tk.StringVar()
        self.top_is_open = False
        self.parent = master
        self.weather_reports = None
        self.globals = None
        self.requester = None
        self.reports_fabric = None
        self.w_window = None
        self.timeout = -1
        self.config = configparser.ConfigParser()
        self.config.read(config_file_path, "windows-1250")
        self.parent.title("Pogodna Pogoda")
        self.parent.iconbitmap("images/weather.ico")
        tk.Frame.__init__(self, master)
        self.parent = master
        self.parent.protocol("WM_DELETE_WINDOW", self.file_quit)

        self.geometry_base = self.config["DEFAULT"]["base_geometry"]
        self.parent.geometry(self.geometry_base)
        self.config_json = self.config["DEFAULT"]["config_file"]
        self.def_lat = self.config["DEFAULT"]["def_lat"]
        self.def_long = self.config["DEFAULT"]["def_long"]
        WeatherApp.set_colours(self.config["DEFAULT"]["theme_1"], self.config["DEFAULT"]["theme_2"],
                               self.config["DEFAULT"]["theme_3"])
        self.create_status()

        try:
            self.read_config_from_json()
        except FileNotFoundError as e:
            self.set_statusbar(f"Brak pliku: {e.__str__()}")
        self.create_base_menu()

        self.create_working_window()
        self.add_menu_favorites()
        self.add_menu_options()
        # self.place_type_setter = 2  # 0 - fav, 1 - coord, 3 - search name
        self.set_place_type_setter(int(self.config["DEFAULT"]["place_type_setter"]))
        self.initiate_data_collection()

        self.parent.columnconfigure(0, weight=999)
        self.parent.columnconfigure(1, weight=1)
        self.parent.rowconfigure(0, weight=1)
        self.parent.rowconfigure(1, weight=9999)
        self.parent.rowconfigure(2, weight=1)

    @classmethod
    def set_colours(cls, theme_1, theme_2, theme_3):
        cls.theme_1 = theme_1
        cls.theme_2 = theme_2
        cls.theme_3 = theme_3

    def write_config_to_json(self):
        data = {"values_request": self.values_request,
                "label_names": self.label_names,
                "favorite_loc": self.favorite_loc,
                "theme_1": self.theme_1,
                "theme_2": self.theme_2,
                "def_loc_name": self.def_loc_name,
                "numb_of_default_loc_name": self.numb_of_default_loc_name,
                "link_temp": RequestApiCreator.link_temp,
                "requests_temp": RequestApiCreator.requests_temp
                }
        with open(self.config_json, 'w') as fj:
            json.dump(data, fj, indent=2)

    def read_config_from_json(self):
        with open(self.config_json) as fj:
            data = json.load(fj)
            self.values_request = data["values_request"]
            self.label_names = data["label_names"]
            self.favorite_loc = data["favorite_loc"]
            self.theme_1 = data["theme_1"]
            self.theme_2 = data["theme_2"]
            self.def_loc_name = data["def_loc_name"]
            self.numb_of_default_loc_name = data["numb_of_default_loc_name"]
            RequestApiCreator.requests_temp = data["requests_temp"]
            RequestApiCreator.values_ctrl = data["values_request"]
            RequestApiCreator.link_temp = data["link_temp"]

    def set_place_type(self):
        if self.requester is None or self.place_type_setter < 0 or self.place_type_setter > 2:
            return False
        if self.place_type_setter == 0:
            selected_name = self.w_window.fav_loc_selector.get()
            if selected_name == "{Brak}":
                tkinter.messagebox.askretrycancel("", "Wybierz odpowiedni element z ulubionych", parent=self.parent)
                return False
            lat, long = self.favorite_loc[selected_name]
            self.requester.set_coord(lat, long)
            self.w_window.set_entry_to_show(lat, long, "")
            return True

        if self.place_type_setter == 1:
            try:
                self.requester.set_coord(self.w_window.latitude.get(), self.w_window.longitude.get())
            except tkinter.TclError:
                tkinter.messagebox.askretrycancel("", "Błędny typ współrzędnych", parent=self.parent)
                return False
            lat, long = self.requester.get_coord()
            self.w_window.set_entry_to_show(lat, long, "")
            return True

        if self.place_type_setter == 2:
            self.requester.set_by_location(self.w_window.search_name.get())
            if self.requester.location is None:
                tkinter.messagebox.askretrycancel("", "Nieodpowiednie hasło wyszukiwania", parent=self.parent)
                return False
            lat, long = self.requester.get_coord()
            self.w_window.set_entry_to_show(lat, long, self.w_window.search_name.get())
            return True

    def initiate_data_collection(self):
        self.requester = RequestApiCreator()
        self.requester.append_many_hour_day("hourly", self.values_request["hourly"])
        self.requester.append_many_hour_day("daily", self.values_request["daily"])
        self.requester.set_param("current_weather", True)
        self.reports_fabric = WeatherReportFabric()
        self.refresh_results()

    def refresh_results(self):

        if self.timeout != -1:
            curr_time = int(round(time.time() * 1000))
            self.timeout = curr_time - self.timeout
            if self.timeout < 1500:
                self.set_statusbar(f"Zbyt dużo rządań połączeń z API - timeout: {self.timeout / 1000}s")
                time.sleep(self.timeout / 1000)
                self.timeout = int(round(time.time() * 1000))
                return

        if not self.set_place_type():
            return
        if self.place_type_setter == 2:
            self.set_statusbar(f"Wyniki dla: {self.requester.search_result}")
        elif self.place_type_setter == 1:
            self.set_statusbar(f"Wyniki dla: {self.requester.get_coord()}")
        else:
            self.set_statusbar(f"Wyniki dla: {self.w_window.fav_loc_selector.get()}={self.requester.get_coord()}")

        self.requester.create()
        self.reports_fabric.weather_data = self.requester.obtain_data()
        self.reports_fabric.produce()
        self.globals, self.weather_reports = self.reports_fabric.return_reports()
        self.w_window.init_data(self.weather_reports, self.globals)
        self.timeout = int(round(time.time() * 1000))

    def create_status(self):
        self.statusbar = tk.Label(self.parent, text="Linia statusu...",
                                  anchor=tkinter.W)
        self.statusbar.after(5000, self.clear_statusbar)
        self.statusbar.grid(row=2, column=0, columnspan=2,
                            sticky=tkinter.EW)
        pass

    def set_statusbar(self, txt):
        self.statusbar["text"] = txt

    def clear_statusbar(self):
        self.statusbar["text"] = ""

    def create_base_menu(self):
        self.menubar = tk.Menu(self.parent)
        self.parent["menu"] = self.menubar
        fileMenu = tk.Menu(self.menubar)
        for label, command, shortcut_text, shortcut in (
                ("Info", self.file_info, "Ctrl+S", "<Control-s>"),
                (None, None, None, None),
                ("Wyjście", self.file_quit, "Ctrl+Q", "<Control-q>")):
            if label is None:
                fileMenu.add_separator()
            else:
                fileMenu.add_command(label=label, underline=0,
                                     command=command, accelerator=shortcut_text)
                self.parent.bind(shortcut, command)
        self.menubar.add_cascade(label="Plik", menu=fileMenu, underline=0)
        pass

    def set_place_type_setter(self, val):
        if val == 0:
            info = "z ulubionych"
        elif val == 1:
            info = "ze współrzędnych"
        else:
            info = "z wyszukiwania"
        self.set_statusbar(f"Opcja wyboru: {info}")
        self.place_type_setter = val
        self.w_window.insert_colour_enable(self.place_type_setter)

    def add_menu_options(self):
        fileMenu = tk.Menu(self.menubar)
        for label, command in (
                ("Z ulubionych", lambda: self.set_place_type_setter(0)),
                ("Ze współrzędnych", lambda: self.set_place_type_setter(1)),
                ("Z wyszukiwania", lambda: self.set_place_type_setter(2))):
            if label is None:
                fileMenu.add_separator()
            else:
                fileMenu.add_command(label=label, underline=0,
                                     command=command)
        self.menubar.add_cascade(label="Opcje wyboru", menu=fileMenu, underline=0)
        pass

    def add_menu_favorites(self):
        fileMenu = tk.Menu(self.menubar)
        for label, command in (
                ("Dodaj", self.choose_to_add),
                ("Usuń", self.choose_to_rm)):
            if label is None:
                fileMenu.add_separator()
            else:
                fileMenu.add_command(label=label, underline=0,
                                     command=command)
        self.menubar.add_cascade(label="Ulubione", menu=fileMenu, underline=0)
        pass

    def default_loc_name_gen(self):
        temp = "MojaLokalizacja"
        if len(self.favorite_loc) == 0:
            self.numb_of_default_loc_name = 0

        to_ret = temp + str(self.numb_of_default_loc_name)
        return to_ret

    def choose_to_add(self):
        if self.top_is_open:
            return
        if len(self.favorite_loc) == 0:
            self.set_statusbar("Brak ulubionych lokalizacji")
            return

        self.top_is_open = True
        win = tk.Toplevel(self.parent, height=300, width=300)
        win.title("Dodawanie")
        win.iconbitmap("images/weather.ico")
        win.protocol("WM_DELETE_WINDOW", lambda: self.set_top_close(win))
        label = tk.Label(win, text="Podaj nazwę pod jaką chcesz zapisać bierzącą lokalizację:")
        label.pack(anchor=tk.CENTER, padx=50, pady=5)
        tk.Entry(win, textvariable=self.sel_add_name, width=label["width"]).pack(anchor=tk.CENTER, pady=5)
        self.sel_add_name.set(self.default_loc_name_gen())
        x = self.parent.winfo_x()
        y = self.parent.winfo_y()
        win.geometry("+%d+%d" % (x + 300, y + 300))
        win.wm_transient(self.parent)
        tk.Button(win, text="Dodaj",
                  command=lambda: self.add_fav(win)).pack(anchor=tk.CENTER, padx=50, pady=5)

    def add_fav(self, win):
        self.set_top_close(win)
        name = self.sel_add_name.get()
        if name in self.favorite_loc.keys():
            self.set_statusbar("Błąd dodawania: istnieje już lokalizacja o podanej nazwie")
            return
        coord = self.w_window.latitude.get(), self.w_window.longitude.get()
        if coord in self.favorite_loc.values():
            self.set_statusbar("Błąd dodawania: istnieje już lokalizacja o podanych współrzędnych")
            return
        self.numb_of_default_loc_name = self.numb_of_default_loc_name + 1
        self.favorite_loc[name] = coord
        self.w_window.set_fav_combo(list(self.favorite_loc.keys()))
        self.set_statusbar(f"Dodano do ulubionych: {name}={coord}")

    def choose_to_rm(self):
        if self.top_is_open:
            return
        if len(self.favorite_loc) == 0:
            self.set_statusbar("Brak ulubionych lokalizacji")
            return

        self.top_is_open = True
        win = tk.Toplevel(self.parent, height=300, width=300)
        win.title("Usuwanie")
        win.iconbitmap("images/weather.ico")
        win.protocol("WM_DELETE_WINDOW", lambda: self.set_top_close(win))
        tk.Label(win, text="Wybierz lokację do usunięcia").pack(anchor=tk.CENTER, padx=50, pady=5)
        x = self.parent.winfo_x()
        y = self.parent.winfo_y()
        win.geometry("+%d+%d" % (x + 300, y + 300))
        win.wm_transient(self.parent)
        com_box = ttk.Combobox(win, textvariable=self.sel_opt_rm)
        com_box['values'] = tuple(self.favorite_loc)
        com_box.set("-------")
        com_box.pack(anchor=tk.CENTER, padx=50, pady=5)
        com_box.bind('<<ComboboxSelected>>',
                     lambda e: self.set_statusbar(f"Wybrano do usunięcia: {self.sel_opt_rm.get()}"))
        tk.Button(win, text="Usuń",
                  command=lambda: self.rem_fav(win)).pack(anchor=tk.CENTER, padx=50, pady=5)

    def set_top_close(self, window=None):
        self.top_is_open = False
        if window is not None:
            window.destroy()
            window.update()

    def rem_fav(self, win):
        self.set_top_close(win)
        to_rm_key = self.sel_opt_rm.get()
        del self.favorite_loc[to_rm_key]
        self.w_window.set_fav_combo(list(self.favorite_loc.keys()))
        self.set_statusbar(f"Usunięto: {to_rm_key}")

    def file_quit(self, event=None):
        reply = tkinter.messagebox.askyesno(
            "",
            "Czy naprawdę chcesz wyjść z programu?", parent=self.parent)
        if reply:
            geometry = self.parent.winfo_geometry()
            self.config["DEFAULT"]["base_geometry"] = geometry
            self.write_config_to_json()
            with open(config_file_path, 'w') as config_file:
                self.config.write(config_file)
            self.parent.destroy()
        pass

    def file_info(self):
        self.set_statusbar(
            "Stworzone przez: Krzysztof Wróblewski 2022, oprogramowanie open-source, nieodpłatne, wersja 1.0")
        pass

    def create_working_window(self):
        self.w_window = WorkingWindow(self, master=self.parent)
        self.w_window.grid(row=1, column=0, columnspan=1, rowspan=1, sticky=NSEW)
        pass

    def refresh(self):
        pass

    def add_to_fav(self):
        pass

    def location_search_options(self):
        pass


class WorkingWindow(tk.Frame):

    def __init__(self, weather_app, master=None):
        tk.Frame.__init__(self, master, padx=10, pady=10, bg=WeatherApp.theme_1)

        self.fav_combo_box = None
        self.weather_app = weather_app
        self.style = ttk.Style()
        self.latitude = tk.DoubleVar()
        self.latitude.set(self.weather_app.def_lat)
        self.longitude = tk.DoubleVar()
        self.longitude.set(self.weather_app.def_long)
        self.search_name = tk.StringVar()
        self.search_name.set(self.weather_app.def_loc_name)
        self.fav_loc_selector = tk.StringVar()
        self.past_weathercode = -1

        self.weather_fr = None
        self.weather_im_label = None
        self.parent = master

        self.create_weather_image_frame()
        self.weather_fr.grid(row=0, column=0, columnspan=1, rowspan=1, sticky=tk.NSEW)
        self.create_input_frame()
        self.option_frame.grid(row=0, column=1, columnspan=1, rowspan=1, sticky=tk.NSEW)
        self.create_date_input_frame()
        self.date_frame.grid(row=0, column=2, columnspan=1, rowspan=3, sticky=tk.NSEW)
        self.create_temperature_brief()
        self.temper_brief.grid(row=1, column=0, columnspan=2, rowspan=1, sticky=tk.NSEW)
        self.create_add_weather_data()
        self.extra_weather.grid(row=2, column=0, columnspan=2, rowspan=1, sticky=tk.NSEW)

        self.columnconfigure(0, weight=1)
        self.columnconfigure(1, weight=1)
        self.columnconfigure(2, weight=1)
        self.rowconfigure(0, weight=1)
        self.rowconfigure(1, weight=0)
        self.rowconfigure(2, weight=999)

    def insert_colour_enable(self, type):

        if type == 0:
            self.style.configure("TCombobox", fieldbackground="red")
            self.search_entry.config(bg="white")
            self.lat_entry.config(bg="white")
            self.long_entry.config(bg="white")
        elif type == 1:
            self.lat_entry.config(bg=WeatherApp.theme_3)
            self.long_entry.config(bg=WeatherApp.theme_3)
            self.search_entry.config(bg="white")
        else:
            self.search_entry.config(bg=WeatherApp.theme_3)
            self.lat_entry.config(bg="white")
            self.long_entry.config(bg="white")

    def create_add_weather_data(self):
        self.extra_weather = tk.LabelFrame(self, text="Dodatkowe dane pogodowe", pady=10, padx=10)
        self.by_hour_present = tk.LabelFrame(self.extra_weather, text="Parametry godzinowe", pady=10, padx=10)
        self.by_day_present = tk.LabelFrame(self.extra_weather, text="Parametry dzienne", pady=10, padx=10)

        self.extras_hourly = self.create_label_row_series(self.by_hour_present, self.weather_app.label_names["hourly"],
                                                          0, tk.W)
        self.extras_val_hour = self.create_label_row_series(self.by_hour_present,
                                                            ["----"] * len(self.weather_app.label_names["hourly"]),
                                                            1, tk.E)
        self.extras_daily = self.create_label_row_series(self.by_day_present, self.weather_app.label_names["daily"], 0,
                                                         tk.W)

        self.extras_val_day = self.create_label_row_series(self.by_day_present,
                                                           ["----"] * len(self.weather_app.label_names["daily"]),
                                                           1, tk.E)

        self.create_label_request_dict()

        self.by_day_present.columnconfigure(0, weight=1)
        self.by_day_present.columnconfigure(1, weight=1)

        self.by_hour_present.columnconfigure(0, weight=1)
        self.by_hour_present.columnconfigure(1, weight=1)

        self.by_hour_present.grid(row=0, column=0, sticky=tk.NSEW)
        self.by_day_present.grid(row=0, column=1, sticky=tk.NSEW)
        self.extra_weather.columnconfigure(0, weight=1)
        self.extra_weather.columnconfigure(1, weight=1)
        self.extra_weather.rowconfigure(0, weight=1)

    def init_data(self, weather_rep, globals):

        self.data = weather_rep
        self.globs = globals
        self.def_time = globals["current_weather"]["time"]
        self.def_time = self.to_date_obj(self.def_time)
        def_time = self.def_time
        self.set_time_picker(def_time.time().hour)
        self.set_calendar_period(def_time.date(), def_time.date() + dt.timedelta(days=6))
        self.set_calendar_default(def_time)
        self.fill_interface(0, def_time.time().hour)

    def fill_interface(self, index_date, index_time):
        if index_date < 0 or index_date > 6 or index_time < 0 or index_time > 23:
            raise Exception("wrong index to fill data")
        report = self.data[index_date]
        hrs_report_val = report.get_data_by_index(index_time)
        day_report_val = report.properties
        for i in range(len(self.extras_daily)):
            text = day_report_val[self.label_req_dict["daily"][self.extras_daily[i]["text"]]]
            if self.extras_daily[i]["text"] in ["Wschód (godz):", "Zachód (godz):"]:
                text = self.to_date_obj(text).time()
            self.extras_val_day[i].config(text=text)

        for i in range(len(self.extras_hourly)):
            text = hrs_report_val[self.label_req_dict["hourly"][self.extras_hourly[i]["text"]]]
            self.extras_val_hour[i].config(text=text)

        self.set_max_min_temp(day_report_val["temperature_2m_max"], day_report_val["temperature_2m_min"])
        if self.past_weathercode != day_report_val["weathercode"]:
            self.switch_weather_pic(day_report_val["weathercode"])
            self.past_weathercode = day_report_val["weathercode"]

    def switch_weather_pic(self, weathercode):
        prefix = "images/weather"
        prev = weathercode
        if weathercode > 3:
            weathercode = int(weathercode / 10)
        else:
            weathercode = int(weathercode)
        print(f"weathercode={weathercode}, {prev}")
        fileslist = [f for f in os.listdir(prefix) if f.endswith(f'{weathercode}.png')]
        if len(fileslist) > 1:
            if prev > 82:
                fileslist = [f for f in fileslist if f.endswith(f'w_{weathercode}.png')]
            else:
                fileslist = [f for f in fileslist if f.endswith(f'n_{weathercode}.png')]

        self.set_weather_icon(prefix + "/" + fileslist[0])
        pass

    def to_date_obj(self, date_):
        date_ = date_.replace("T", " ")
        date_ = datetime.strptime(date_, "%Y-%m-%d %H:%M")
        # print(date_)
        return date_

    def create_label_request_dict(self):
        req = self.weather_app.values_request.copy()  # rem hour: weathercode
        names = self.weather_app.label_names.copy()  # rem daily: weathercode temperature_2m_min temperature_2m_max

        req["daily"] = [elem for elem in req["daily"] if
                        elem not in ["weathercode", "temperature_2m_min", "temperature_2m_max"]]
        req["hourly"] = [elem for elem in req["hourly"] if elem != "weathercode"]
        self.label_req_dict = {"daily": dict(zip(names["daily"], req["daily"])),
                               "hourly": dict(zip(names["hourly"], req["hourly"]))}
        # print(self.label_req_dict)

    def create_label_row_series(self, parent, names, column_index, stickiness):
        label_list = []
        row_numb = len(names)

        for row_index in range(row_numb):
            label_to_append = tk.Label(parent, text=names[row_index], pady=2)
            label_to_append.grid(column=column_index, row=row_index, sticky=stickiness)
            label_list.append(label_to_append)
        return label_list

    def create_temperature_brief(self):
        self.temper_brief = tk.LabelFrame(self, text="Temperatury:", pady=10, padx=10)
        tk.Label(self.temper_brief, font=("Times", 16), text="MAX: ").grid(row=0, column=0, sticky=tk.NSEW)
        self.max_temp = tk.Label(self.temper_brief, font=("Times", 16), text="-------")
        self.max_temp.grid(row=0, column=1, sticky=tk.NSEW)
        # tk.Label(self.temper_brief, font=("Times", 16), pady=10, padx=50).grid(row=0, column=2, sticky=tk.NSEW)
        tk.Label(self.temper_brief, font=("Times", 16), text="MIN: ").grid(row=0, column=2, sticky=tk.NS)
        self.min_temp = tk.Label(self.temper_brief, font=("Times", 16), text="-------")
        self.min_temp.grid(row=0, column=3, sticky=tk.NSEW)

        self.temper_brief.columnconfigure(0, weight=1)
        self.temper_brief.columnconfigure(1, weight=1)
        self.temper_brief.columnconfigure(2, weight=1)
        self.temper_brief.columnconfigure(3, weight=1)

    def set_max_min_temp(self, max, min):
        self.max_temp.configure(text=f"{max}℃")
        self.min_temp.configure(text=f"{min}℃")

    def create_weather_image_frame(self):
        self.weather_fr = tk.LabelFrame(self, text="Pogoda", pady=10, padx=10, bg=WeatherApp.theme_2)
        img_to_open = Image.open("images/weather/clear_sky_0.png")
        img = ImageTk.PhotoImage(img_to_open.resize((200, 200)))
        self.weather_im_label = tk.Label(self.weather_fr, image=img, compound=tk.LEFT, background=WeatherApp.theme_2)
        self.weather_im_label.image = img
        self.weather_im_label.pack(anchor=tk.CENTER)

    def set_weather_icon(self, img_path):
        img_to_open = Image.open(img_path)
        img = ImageTk.PhotoImage(img_to_open.resize((200, 200)))
        self.weather_im_label.configure(image=img)
        self.weather_im_label.image = img
        self.weather_im_label.pack(anchor=tk.CENTER)

    def create_input_frame(self):
        self.option_frame = tk.LabelFrame(self, text="Podaj dane", pady=10, padx=10)
        tk.Label(self.option_frame, text='Wybierz z ulubionych:').pack()
        fav_combo_box = ttk.Combobox(self.option_frame, textvariable=self.fav_loc_selector)
        val_list = list(self.weather_app.favorite_loc.keys())
        val_list.sort()
        values = tuple(["{Brak}"] + val_list)
        fav_combo_box['values'] = values
        fav_combo_box.set('{Brak}')
        fav_combo_box.pack()
        # fav_combo_box.bind('<<ComboboxSelected>>', lambda e: self.get_fav_loc())
        self.fav_combo_box = fav_combo_box

        tk.Label(self.option_frame, text='Podaj szerokość geograficzną (NS):').pack()
        lat_entry = tk.Entry(self.option_frame, textvariable=self.latitude)
        lat_entry.pack()
        self.lat_entry = lat_entry
        tk.Label(self.option_frame, text='Podaj długość geograficzną (WE):').pack()
        long_entry = tk.Entry(self.option_frame, textvariable=self.longitude)
        long_entry.pack()
        self.long_entry = long_entry
        tk.Label(self.option_frame, text='Wyszukiwanie po nazwie:').pack()
        self.search_entry = tk.Entry(self.option_frame, textvariable=self.search_name)
        self.search_entry.pack()
        tk.Button(self.option_frame, text="Pokaż", command=self.get_inputs).pack()

    def get_inputs(self):
        print("Save inputs here")
        self.weather_app.refresh_results()
        pass

    # def get_fav_loc(self):
    #     print("Process selected favorite localisations here")
    #     pass

    def set_fav_combo(self, list_):
        list_.sort()
        self.fav_combo_box["values"] = tuple(["{Brak}"] + list_)
        self.fav_combo_box.set('{Brak}')

    def set_entry_to_show(self, lat, long, loc_name):
        self.latitude.set(lat)
        self.longitude.set(long)
        self.search_name.set(loc_name)

    def create_date_input_frame(self):
        self.date_frame = tk.LabelFrame(self, text="Zakres czasowy", pady=10, padx=10)
        self.cal = Calendar(self.date_frame, font="Arial 14", selectmode='day', locale='pl_PL',
                            disabledforeground='yellow')
        self.cal.pack()
        time_frame = tk.LabelFrame(self.date_frame, text="Wybierz godzinę (używając scrolla):", pady=5, padx=10)

        self.time_picker = SpinTimePickerModern(time_frame)
        self.ok_button = tk.Button(time_frame, text="Zatwierdź zakres czasowy", command=self.accept_time_period,
                                   width=30)

        self.time_picker.addHours24()
        self.time_picker.configureAll(bg=WeatherApp.theme_1, height=1, width=2, fg="#ffffff", font=("Times", 16),
                                      hoverbg=WeatherApp.theme_1,
                                      hovercolor=WeatherApp.theme_1, clickedbg=WeatherApp.theme_1,
                                      clickedcolor=WeatherApp.theme_2)
        self.time_picker.grid(row=0, column=0)
        tk.Label(time_frame, font=("Times", 16), background=WeatherApp.theme_1, foreground="white", text=": 00").grid(
            row=0, column=1)
        self.ok_button.grid(row=0, column=2)
        self.time_picker.set24Hrs(0)
        time_frame.pack(padx=10, pady=10)
        tk.Button(self.date_frame, text="Ustaw bieżący czas dla lokacji", command=self.set_now,
                  width=30).pack(anchor=tk.CENTER)

    def set_now(self):
        self.set_time_picker(self.def_time.hour)
        self.set_calendar_default(self.def_time)
        self.fill_interface(0, self.def_time.hour)

    def set_calendar_period(self, min, max):
        self.cal.config(mindate=min, maxdate=max)

    def set_calendar_default(self, default_date):
        self.cal.selection_set(default_date)

    def set_time_picker(self, hrs):
        self.time_picker.set24Hrs(hrs)

    def accept_time_period(self):
        # print("Show selected weather forecast by date/time")
        index_day = self.cal.selection_get().day
        first_day = self.def_time.day
        index_day = index_day - first_day
        # print(self.time_picker.hours24())
        # print("------")
        # print(self.def_time)
        # print(self.def_time)
        # print(self.cal.selection_get().day)
        # print(index_day)
        self.fill_interface(index_day, self.time_picker.hours24())
        pass


if __name__ == '__main__':
    root = tk.Tk()
    app = WeatherApp(master=root)
    app.mainloop()
    pass
