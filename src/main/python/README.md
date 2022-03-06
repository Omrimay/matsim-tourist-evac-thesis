## python code:
### pre-simulation processing
These files should be adjusted according to the input files structure and format.
#### createNetwork.py
Takes **links.csv** and **nodes.csv** and generate a complaint xml network file.

#### createTrips.py
Takes odMatrix.csv and generate a compliant xml plans file.

### post-simulation processing
**readPlans.py** and **readEvents.py** generate flat csv files from the corresponding trips and events output files.
