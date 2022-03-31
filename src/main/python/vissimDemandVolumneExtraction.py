import xml.etree.ElementTree as ET

import pandas as pd

vissim = ET.parse("/Users/fouriep/Documents/IdeaProjects/matsim-tourist-evac/dropbox/MNL_TimePressure.inpx")
vroot = vissim.getroot()

pedestrianInputs = vroot.findall('.//pedestrianInput')
cols = ['area', 'vol1', 'vol2', 'vol3']
rows = []
for pi in pedestrianInputs:
    loc = pi.find("location")
    print(loc.attrib.get('area'))
    pos_on_area = loc.find('posOnArea').attrib
    vols = pi.findall('.//timeIntervalPedVolume')
    rows.append({'area':loc.attrib.get('area'), 'vol1':int(vols[0].attrib.get('volume')), 'vol2':int(vols[1].attrib.get('volume')), 'vol3':int(vols[2].attrib.get('volume'))})


volumes_df = pd.DataFrame(rows, columns=cols)

areas = vroot.findall('.//area')
cols = ['area', 'x', 'y']
rows = []
for area in areas:
    area_no = area.attrib.get('no')
    sumx = 0
    sumy = 0
    count = 0
    for point in area.findall('.//point'):
        sumx += float(point.attrib.get('x'))
        sumy += float(point.attrib.get('y'))
        count += 1
    rows.append({'area': area_no, 'x': (sumx / count), 'y': sumy / count})

coords  = pd.DataFrame(rows, columns=cols)


output = coords.merge(volumes_df,left_on='area',right_on='area')

output.to_csv('/Users/fouriep/Documents/IdeaProjects/matsim-tourist-evac/dropbox/vissimdemandvolumes.csv', index=None, header=True )