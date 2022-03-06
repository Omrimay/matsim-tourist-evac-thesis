import os
import csv
import sys
import xml.etree.ElementTree as ET
from xml.dom import minidom
import pandas as pd

#links_df.id = links_df.id.astype(str)
#print(links.head(5))
#print(links['length'])
network = ET.Element('network')
network.set('name','Conegliano network')

nodes_df = pd.read_csv(r"C:\Users\Yuval Hadas\Documents\MATSim\SHP3\nodes.csv",dtype={"N" : "object"})
nodes = ET.SubElement(network, 'nodes')
for index, row in nodes_df.iterrows():
    node = ET.SubElement(nodes, 'node')
    node.set('id',str(row['N']))
    node.set('x', str(row['X']))
    node.set('y', str(row['Y']))

links_df = pd.read_csv(r"C:\Users\Yuval Hadas\Documents\MATSim\SHP3\links.csv",dtype={"id" : "object"})
links_df['capacity'].astype(int)
links = ET.SubElement(network, 'links')
links.set('capperiod', '01:00:00')
for index, row in links_df.iterrows():
    link = ET.SubElement(links, 'link')
    link.set('id',str(row['id']))
    link.set('from', str(row['A']))
    link.set('to', str(row['B']))
    link.set('length', str(row['length']))
    link.set('freespeed', str(row['freespeed']))
    link.set('capacity', str(row['capacity']*100))
    link.set('permlanes', '1.0')
    link.set('modes', 'car')
    # if int(row['id']) == 542:
    #     link.set('modes', 'car2')
    # else:
    #     link.set('modes', 'car,car2')

f = open('network20.xml', 'wb')
tree = ET.ElementTree(network)
f.write('<?xml version="1.0" ?>'.encode('utf8'))
f.write('<!DOCTYPE network SYSTEM "http://www.matsim.org/files/dtd/network_v2.dtd">'.encode('utf8'))
tree.write(f, 'utf-8')
