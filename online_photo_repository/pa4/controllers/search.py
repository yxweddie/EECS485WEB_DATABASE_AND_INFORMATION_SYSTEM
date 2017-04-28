import requests
import itertools

from flask import *
from pa4.models import *
from pa4.config import *

search_blue = Blueprint('search', __name__, template_folder='views')
# @search_blue.route('/search/', defaults={'query': ""})
# @search_blue.route('/search/<query>', methods=['POST','GET'])
@search_blue.route('/search', methods=['POST','GET'])
def query_photos():
	display = False
	if request.method == 'GET':
		return render_template('search.html', display = display)
	else:
		display = True
		q  = list(request.form['query'])
		query = ''
		for i in range(len(q)):
			if q[i] != '&':
				query = query + q[i]
		if query :
			print query
			result = requests.get('http://localhost:6326/search?q=' + query)
			result = result.json()
			jList  = result.get('hits')
			count=len(jList)
			lists=[]
			for i in jList:
				print i.get('id')
				seq=i.get('id')
				photo_info = pic_model.get_pic_by_seq(seq)
				lists.append(photo_info)
			return render_template('search.html', count=count, photos=lists, display=display)
		else:
			return render_template('search.html', display = False)
