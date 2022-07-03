from timezonefinder import TimezoneFinder
import requests as req
from geopy.geocoders import Nominatim


class NoWeatherParameterException(Exception):
    pass


class RequestApiCreator:
    link_temp = None # wzór linku do żadania pogody (zserializowany)

    requests_temp = None # wzór configurowalnych parametrów do żadania pogody (zserializowany)

    values_ctrl = None # wzór wartości do żadania pogody (zserializowany)

    def __init__(self):
        self.location = None
        self.search_result = None
        self.requests = RequestApiCreator.get_temp()
        self.req_link = ""
        self.geolocator = Nominatim(user_agent="geoapiExercises")

    @classmethod
    def get_temp(cls):
        return cls.requests_temp.copy()

    def clear_request(self):
        self.requests = RequestApiCreator.get_temp()

    def calc_timezone(self):
        lat = self.requests["latitude"]
        long = self.requests["longitude"]
        if lat is None or long is None or type(lat) != float or type(long) != float:
            return False
        time_find = TimezoneFinder()
        self.requests["timezone"] = time_find.timezone_at(lng=long, lat=lat)
        return True

    def set_coord(self, lat, long):
        self.requests["latitude"] = lat
        self.requests["longitude"] = long
        # self.search_result = self.geolocator.reverse() #???

    def get_coord(self):
        lat = self.requests["latitude"]
        long = self.requests["longitude"]
        return lat, long

    def set_by_location(self, loc_name):
        location = self.geolocator.geocode(loc_name)
        self.location = location
        if location is None:
            return
        print(location.latitude, location.longitude)
        self.search_result = location.raw["display_name"]
        print(self.search_result)
        self.set_coord(location.latitude, location.longitude)

    def set_param(self, key, value):
        if key not in self.requests.keys():
            return False
        self.requests[key] = value
        return True

    def create(self):
        if not self.calc_timezone():
            raise NoWeatherParameterException("No coordinates")
        base = [f"&{key}={self.value_formatter(key, val)}" for key, val in self.requests.items() if
                (val is not None and type(val) != list) or (type(val) == list and val)]
        # base[0] = base[0][1:]
        self.req_link = self.link_temp + "".join(base)
        return self.req_link

    def obtain_data(self):
        if self.req_link == "":
            raise NoWeatherParameterException("Empty link!")
        weather_data = req.get(self.req_link).json()
        return weather_data

    def value_formatter(self, key, val):
        if key == "timezone":
            return val.replace("/", "%2F")
        if key == "daily" or key == "hourly":
            to_rep = [f",{i}" for i in val]
            to_rep[0] = to_rep[0][1:]
            return "".join(to_rep)
        return val

    def append_hour_day(self, key, value):
        if key not in self.values_ctrl.keys() or value not in self.values_ctrl[key]:
            raise NoWeatherParameterException(f"{key}={value}")
        if value not in self.requests[key]:
            self.requests[key].append(value)
        return True

    def append_many_hour_day(self, key, values):
        if type(values) != list:
            raise NoWeatherParameterException(f"Wrong type od values list: {type(values)}")
        for value in values:
            self.append_hour_day(key, value)

# if __name__ == '__main__':
#     requester = RequestApiCreator()
#     # req.requests["latitude"] = 2
#     # print(req.requests)
#     # print(RequestApiCreator.get_temp())
#     # requester.set_coord(51.1, 17.0333)
#     requester.set_by_location("Wrocław")
#
#     print(requester.append_hour_day("hourly", "temperature_2m"))
#     # print(requester.append_hour_day("hourly", "pressure_msl"))
#     print(requester.append_hour_day("daily", "temperature_2m_max"))
#     requester.set_param("current_weather", True)
#     print(requester.create())
#     for key, val in requester.obtain_data().items():
#         print(f"{key} = {val}")
#     # print(type(req.requests["hourly"]) == list and len(req.requests["hourly"]) > 0)
#     # for key, val in req.requests.items():
#     #     print(val)
#     #     print((val is not None and type(val) != list) or (type(val) == list and val))
