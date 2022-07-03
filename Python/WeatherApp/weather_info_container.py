from request_api_handler import RequestApiCreator


def dict_sub_lister(dict_, begin, end):
    dict_to_return = {}
    for key in dict_.keys():
        if type(dict_[key]) != list:
            continue
        sub_list = dict_[key][begin:end]
        if len(sub_list) == 1:
            sub_list = sub_list[0]
        dict_to_return[key] = sub_list
    return dict_to_return


class WeatherReportFabric:
    def __init__(self, weather_data=None):
        self.weather_data = weather_data
        self.global_options = {}
        self.reports = []

    def produce(self):
        # date = datetime()
        # properties = {temperature_2m_max: 21.0, temperature_2m_min: 9.0 ...}
        # properties_hourly = [{time: 00:00, temperature: 21,3} ...]
        if self.weather_data is None:
            raise Exception("weather_data was not initiated")
        self.reports = []
        daily_index = 0
        begin_index = 0  # included
        end_index = 24  # excluded
        # set global options
        print(self.weather_data["current_weather"])
        for date in self.weather_data["daily"]["time"]:
            weather_rep = WeatherReport()
            weather_rep.date = date  # format?
            weather_rep.properties = dict_sub_lister(self.weather_data["daily"], daily_index, daily_index + 1)
            weather_rep.properties_hourly = dict_sub_lister(self.weather_data["hourly"], begin_index, end_index)
            self.reports.append(weather_rep)
            # print(f"{begin_index} - {end_index}")
            begin_index = end_index
            end_index = end_index + 24
            daily_index = daily_index + 1
        self.global_options = {"current_weather": self.weather_data["current_weather"]}



    def return_reports(self):
        if len(self.global_options) == 0 and len(self.reports) == 0:
            return None, None
        return self.global_options, self.reports


class WeatherReport:
    def __init__(self):
        # date = datetime()
        # properties = {temperature_2m_max: 21.0, temperature_2m_min: 9.0 ...}
        # properties_hourly = [{time: 00:00, temperature: 21,3} ...]
        self.date = None
        self.properties = None
        self.properties_hourly = None
        pass

    def get_data_by_index(self, index):
        return dict_sub_lister(self.properties_hourly, index, index + 1)

    def __str__(self):
        return f"date=              {self.date}\n" \
               f"properties=        {self.properties}\n" \
               f"properties_hourly= {self.properties_hourly}\n" \
               f"-------------------------------------------------"


# if __name__ == '__main__':
#     print("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
#     requester = RequestApiCreator()
#     # req.requests["latitude"] = 2
#     # print(req.requests)
#     # print(RequestApiCreator.get_temp())
#     # requester.set_coord(51.1, 17.0333)
#     requester.set_by_location("New York")
#
#     print(requester.append_hour_day("hourly", "temperature_2m"))
#     requester.append_many_hour_day("hourly", ["temperature_2m",
#                                               "relativehumidity_2m",
#                                               "apparent_temperature",
#                                               "pressure_msl",
#                                               "cloudcover",
#                                               "windspeed_10m",
#                                               "winddirection_10m",
#                                               "windgusts_10m",
#                                               "precipitation",
#                                               "weathercode",
#                                               ])
#     # print(requester.append_hour_day("hourly", "pressure_msl"))
#     print(requester.append_hour_day("daily", "temperature_2m_max"))
#     requester.append_many_hour_day("daily", ["temperature_2m_max",
#                                              "temperature_2m_min",
#                                              "apparent_temperature_max",
#                                              "apparent_temperature_min",
#                                              "precipitation_sum",
#                                              "precipitation_hours",
#                                              "weathercode",
#                                              "sunrise",
#                                              "sunset",
#                                              "windspeed_10m_max",
#                                              "windgusts_10m_max",
#                                              "winddirection_10m_dominant"])
#     requester.set_param("current_weather", True)
#     requester.create()
#     fabric = WeatherReportFabric(requester.obtain_data())
#     fabric.produce()
#     for rep in fabric.reports:
#         print(rep)
#         print(rep.get_data_by_index(0))
