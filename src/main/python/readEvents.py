import os
import csv
import sys
import xml.etree.ElementTree as ET
from xml.dom import minidom
import pandas as pd
import numpy as np
import gzip

#events = ET.parse("output_events.xml")
events = ET.parse(gzip.open('../matsim-11.x/Evacuation/output/output_events.xml.gz', 'rb'))
xroot = events.getroot()

df_cols = ["person", "timestamp", "link", "type"]
rows = []
event_types = ["departure", "left link", "entered link", "arrival"]
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
        s_link = event_n.attrib.get("link")
        rows.append({"person": s_person, "timestamp": s_timestamp, "link": s_link, "type": s_type})


events_df = pd.DataFrame(rows, columns=df_cols)
events_df['timestamp'] = events_df['timestamp'].astype(float)
events_df['person'] = events_df['person'].astype(int)
events_df['link'] = events_df['link'].astype(int)
events_sorted_df = events_df.sort_values(by=['person', 'timestamp'])
events_sorted_df['next_timestamp'] = events_sorted_df['timestamp'].shift(-1)
events_sorted_df['time'] = events_sorted_df['next_timestamp'] - events_sorted_df['timestamp']
events_sorted_df = events_sorted_df.set_index("type")
events_sorted_df = events_sorted_df.drop("left link", axis=0)
events_sorted_df = events_sorted_df.drop("arrival", axis=0)
events_sorted_df = events_sorted_df.drop(columns="next_timestamp")
export_csv = events_sorted_df.to_csv (r'output_events3.csv', index = None, header=True)

trips_df = events_sorted_df.groupby(["person"]).agg(o=('link', 'first'), d=('link', 'last'), t=('time', np.sum), path=('link', list))
export_csv = trips_df.to_csv (r'output_trips3.csv', header=True)
#print(events_sorted_df.dtypes)
#print(events_sorted_df.head(100))