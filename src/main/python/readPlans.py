import os
import csv
import sys
import xml.etree.ElementTree as ET
import numpy as np
from xml.dom import minidom
import pandas as pd
import gzip

scenario = '11'

# Read plans, events and write trips summary and events

# Read plans

#population = ET.parse("output_plans2.xml")
population = ET.parse(gzip.open('../matsim-11.x/Evacuation/output' + scenario + '/output_plans.xml.gz', 'rb'))
xroot = population.getroot()

df_cols = ["person", "mode", "plans", "score", "time", "distance", "route"]
rows = []
#print(f'root {xroot.tag}')
for person_n in xroot:
#    print(person_n.tag)
    if person_n.tag == "person":
        s_person = person_n.attrib.get("id")
        n_plans = 0
        for plan_n in person_n:
            n_plans += 1
            if plan_n.attrib.get("selected") == "yes":
                for act_n in plan_n:
                    if act_n.tag == "leg":
                        s_mode = act_n.get("mode")
                        for route_n in act_n:
                            s_o = route_n.attrib.get("start_link")
                            s_d = route_n.attrib.get("end_link")
                            s_time = route_n.attrib.get("trav_time")
                            s_distance = route_n.attrib.get("distance")
                            s_score =plan_n.attrib.get("score")
                            s_route = route_n.text
                            rows.append({"person": s_person, "mode": s_mode, "plans": n_plans, "score": s_score, "time": s_time, "distance": s_distance, "route": s_route})
#                            rows.append({"person": s_person, "mode": s_mode, "o": s_o, "d": s_d, "plans": n_plans, "time": s_time, "distance": s_distance, "route": s_route})

plans_df = pd.DataFrame(rows, columns=df_cols)
plans_df['person'] = plans_df['person'].astype(int)
#export_csv = trips_df.to_csv (r'output_plans2.csv', index = None, header=True)

# Read events

events = ET.parse(gzip.open('../matsim-11.x/Evacuation/output' + scenario + '/output_events.xml.gz', 'rb'))
xroot = events.getroot()

df_cols = ["person", "timestamp", "link", "type", "status"]
rows = []
event_types = ["departure", "vehicle enters traffic", "left link", "entered link", "arrival"]
#print(f'root {xroot.tag}')
for event_n in xroot:
#    print(person_n.tag)
    s_type = event_n.attrib.get("type")
    if s_type in event_types:
        s_timestamp = event_n.attrib.get("time")
        if s_type == "departure" or s_type == "arrival":
            s_person = event_n.attrib.get("person")
        else:
            s_person = event_n.attrib.get("vehicle")
        if s_type == "departure":
            s_status = "queue"
        elif s_type == "vehicle enters traffic":
            s_status = "traffic o"
        else:
            s_status = "traffic"
        s_link = event_n.attrib.get("link")
        rows.append({"person": s_person, "timestamp": s_timestamp, "link": s_link, "type": s_type, "status": s_status})


events_df = pd.DataFrame(rows, columns=df_cols)
events_df['timestamp'] = events_df['timestamp'].astype(float)
events_df['person'] = events_df['person'].astype(int)
events_df['link'] = events_df['link'].astype(int)
events_sorted_df = events_df.sort_values(by=['person', 'timestamp'])
events_sorted_df['next_timestamp'] = events_sorted_df['timestamp'].shift(-1)
events_sorted_df['next_type'] = events_sorted_df['type'].shift(-1)
events_sorted_df['time'] = events_sorted_df['next_timestamp'] - events_sorted_df['timestamp']
events_sorted_df.loc[events_sorted_df.next_type == 'arrival', 'status'] = 'traffic d'
events_sorted_df = events_sorted_df.set_index("type")
events_sorted_df = events_sorted_df.drop("left link", axis=0)
events_sorted_df = events_sorted_df.drop("arrival", axis=0)
events_sorted_df = events_sorted_df.drop(columns="next_timestamp")
events_sorted_df = events_sorted_df.drop(columns="next_type")

plans2_df = pd.DataFrame()
plans2_df['person'] = plans_df['person']
plans2_df['mode'] = plans_df['mode']
merged_df = pd.merge(events_sorted_df, plans2_df, on='person', how='outer')

export_csv = merged_df.to_csv (r'output_events' + scenario + '.csv', index=None, header=True)

events_sorted_df['time_queue'] = np.where(events_sorted_df['status'] == 'queue', events_sorted_df['time'], 0)
events_sorted_df['time_od_links'] = np.where((events_sorted_df['status'] == 'traffic o') | (events_sorted_df['status'] == 'traffic d'), events_sorted_df['time'], 0)
events_sorted_df['time_traffic'] = np.where(events_sorted_df['status'] == 'traffic', events_sorted_df['time'], 0)

trips_df = events_sorted_df.groupby(["person"]).agg(o=('link', 'first'), d=('link', 'last'), time_queue=('time_queue', np.sum), time_traffic=('time_traffic', np.sum), time_od_links=('time_od_links', np.sum))

# Merge
merged_df = pd.merge(trips_df, plans_df, on='person', how='outer')
# print(merged_df)

# write

export_csv = merged_df.to_csv (r'output_trips' + scenario + '.csv', index=None, header=True)