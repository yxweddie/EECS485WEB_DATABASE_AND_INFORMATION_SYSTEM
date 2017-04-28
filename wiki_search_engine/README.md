## Intro

This project is the implementation of Hits and PageRank algorithm. This is pre steps for buding  the wiki search engine of our Photo Repository.

## Usage

 * PageRank
    - make
    - eecs485pa5p <dvalue> (-k <numiterations> | -converge <maxchange>) <edges file (mining.edges.xml)> <article file (mining.articles.xml)> outputfile"
    - the input articles file will be the mining.articles.xml, which is over 300MB

 * HITS
    - make
    - eecs485pa5h <h value> (-k <numiterations> | -converge <maxchange>) “queries” <input-net-file> <input-inverted-index-file> <output-file>
 
## Testing 
    You will need to run PageRank First and get the inverted Index  then run  the HITS.


## Link to download the files
  * PageRank
	- [Small Wiki graph](http://www-personal.umich.edu/~wangguan/eecs485/small.net)
   	- [Large Wiki graph](http://www-personal.umich.edu/~wangguan/eecs485/large.net.tar.gz)
  * HITS
	- [Medium wiki graph](http://www-personal.umich.edu/~wangguan/eecs485/hits.net)
  
 
