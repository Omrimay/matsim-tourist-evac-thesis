import os
import csv
import sys
import xml.etree.ElementTree as ET
from xml.dom import minidom
import pandas as pd

# with open('odMatrix2.csv') as csv_file:
#     csv_reader = csv.reader(csv_file, delimiter=',')

OD_df = pd.read_csv(r"C:\Users\Yuval Hadas\Documents\MATSim\SHP3\odMatrix.csv", dtype={"id": "object"})
od_count = 0
plans = ET.Element('plans')
for index, row in OD_df.iterrows():
    od_count += 1
    demand_count = 0
    while demand_count < int(row['demand']):
            demand_count += 1
            temp_person = od_count*1000+demand_count
            person = ET.SubElement(plans, 'person')
            person.set('id',str(temp_person))
            plan = ET.SubElement(person,'plan')
            act = ET.SubElement(plan,"act")
            act.set('type','dummy')
            act.set('x',str(row['XA']))
            act.set('y',str(row['YA']))
            act.set('end_time','06:00:00')
            leg = ET.SubElement(plan,'leg')
            O = [124, 125, 126, 127, 128, 129, 138, 139, 143]
            if row['A'] in O and row['B'] == 5:
                leg.set('mode', 'car2')
            else:
                leg.set('mode', 'car')
            # leg.set('mode', 'car')
            act = ET.SubElement(plan,"act")
            act.set('type','dummy')
            act.set('x',str(row['XB']))
            act.set('y',str(row['YB']))

#print (prettify(plans))
f = open('trips11.xml', 'wb')
tree = ET.ElementTree(plans)
f.write('<?xml version="1.0" ?>'.encode('utf8'))
f.write('<!DOCTYPE plans SYSTEM "http://www.matsim.org/files/dtd/plans_v4.dtd">'.encode('utf8'))
tree.write(f, 'utf-8')
#ET.ElementTree(tree).write(f, 'utf-8')