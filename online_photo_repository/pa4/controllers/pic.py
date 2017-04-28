import itertools

from flask import *
from pa4.config import *
from pa4.models import *

pic_blue = Blueprint('pic', __name__, template_folder='views')


@pic_blue.route('/')
@pic_blue.route('/<int:pic_id>/<int:album_id>')
def picture(pic_id=-1, album_id=-1):
	if (album_id == -1) or (pic_id == ''):
		missing = "you miss pick a specific albumId or user to view the pic"
		return render_template('404.html', missing=missing)

	pictures = album_model.get_pic_of_album(album_id)
	i = 0
	currentIndex = 0
	preIndex = 0
	nextIndex = 0
	picLen = len(pictures)
	for pic in pictures:
		if pic[3] == pic_id:
			currentIndex = i
			break
		i += 1

	if picLen <= 1:
		preIndex = nextIndex = currentIndex
	else:
		if currentIndex == 0:
			preIndex = picLen - 1
			nextIndex = currentIndex + 1
		elif currentIndex == (picLen - 1):
			preIndex = currentIndex - 1
			nextIndex = 0
		else:
			preIndex = currentIndex - 1
			nextIndex = currentIndex + 1

	album_info = list(itertools.chain(*album_model.get_album(album_id)))
	user = list(itertools.chain(*main_model.get_specific_user(album_info[4])))
	valid_result = list(itertools.chain(*album_model.select_user_from_albumaccess(album_id, user[0])))[0]
	# full:show all, part:show pic only
	if user_model.is_login():
		session_username = session['username']
		album_username = user[0]
		if session_username == album_username:
			status = 'full'
		else:
			album_access = album_info[5]
			if album_access == 'public':
				status = 'part'
			else:
				if int(valid_result) == 1:
					status = 'part'
				else:
					status = 'redirect'
	else:
		album_access = album_info[5]
		if album_access == 'public':
			status = 'part'
		else:
			status = 'redirect'
	if status == 'redirect':
		return redirect(url_for('user.login'))
	else:
		return render_template('pic.html', pageTile='Views the photo', preIndex=preIndex, currentIndex=currentIndex,
							   nextIndex=nextIndex, pictures=pictures, album_id=album_id, url_prefix=Home_prefix,
							   check_user=status)


# @pic_blue.route('/caption', methods=['GET', 'POST'])
# def modify_pic_caption():
# 	picid = request.json['picid']
# 	caption = request.json['caption']
# 	result = pic_model.update_pic_caption(picid, caption)
# 	return str(result)

@pic_blue.route('/caption', methods=['GET'])
#@pic_blue.route('/caption/<string:picid>', methods=['GET'])
def pic_caption_get():
	"""
	Expects URL query parameter with picid.
	Returns JSON with the picture's current caption or error.
	{
		"caption": "current caption"
	}
	{
		"error": "error message",
		"status": 422
	}
	"""
	picid = request.args.get('id')
	if not picid:
		response = json.jsonify(error='You did not provide an id parameter.', status=404)
		response.status_code = 404
		return response

	results = pic_model.get_pic_caption(picid)
	#results = pic_model.get_photo(picid)
	caption = None
	if len(results) > 0:
		caption = results[0][2]
		if not caption:
			caption = ''
	else:
		response = json.jsonify(error='Invalid id parameter. The picid does not exist.', status=422)
		response.status_code = 422
		return response
	response = json.jsonify(caption=caption)
	response.status_code = 200
	return response


@pic_blue.route('/caption', methods=['POST'])
def pic_caption_post():
	"""
	Expects JSON POST of the format:
	{
		"caption": "this is the new caption",
		"id": "picid"
	}
	Updates the caption and sends a response of the format
	{
		"caption": "caption",
		"status": 201
	}
	Or if an error occurs:
	{
		"error": "error message",
		"status": 422
	}
	"""
	req_json = request.get_json()
	picid = req_json.get('id')
	caption = req_json.get('caption')

	if (picid is None or picid == "") and caption is None:
		response = json.jsonify(error='You did not provide an id and caption parameter.',
								status=404)
		response.status_code = 404
		return response
	if picid is None or picid == "":
		response = json.jsonify(error='You did not provide an id parameter.', status=404)
		response.status_code = 404
		return response
	if caption is None:
		response = json.jsonify(error='You did not provide a caption parameter.', status=404)
		response.status_code = 404
		return response

	results = pic_model.update_pic_caption(picid, caption)
	if not results:
		response = json.jsonify(error='Invalid id. The picid does not exist.', status=422)
		response.status_code = 422
		return response
	response = json.jsonify(caption=caption, status=201)
	response.status_code = 201
	return response

@pic_blue.route('/favorites', methods = ['GET'])
#@pic_blue.route('/favorites/<string:picid>', methods=['GET'])
#def pic_favorites_get(picid=None):
def pic_favorites_get():
	try:
		picid=request.args.get('id')
		if not picid:
			response = json.jsonify(error='You did not provide an id parameter.', status=404)
			response.status_code = 404
			return response
		results = pic_model.get_photo(picid)
		if results is not None:
			num = favorite_model.get_favorites_num_by_picid(picid)[0][0]
			if num > 0:
				pid = picid
				num_favorites = num
				latest_favorite = favorite_model.get_last_favorites_user(picid)[0][0]
			else:
				pid = picid
				num_favorites = 0
				latest_favorite = ''
		else:
			response = json.jsonify(error='Invalid id parameter. The picid does not exist.', status=422)
			response.status_code = 422
			return response
		response = json.jsonify(id=pid, num_favorites=num_favorites, latest_favorite=latest_favorite)
		response.status_code = 200
		return response
	except Exception as e:
		response = json.jsonify(error="other errors", status=400)
		response.status_code = 400
		return response


@pic_blue.route('/favorites', methods=['POST'])
def pic_favorites_post():
	req_json = request.get_json()
	pid = req_json.get('id')
	username = req_json.get('username')
	try:
		if pid is None or pid == "" :
			if username is None or username == "" :
				response = json.jsonify(error='You did not provide an id and username parameter.', status=404)
				response.status_code = 404
			else:
				response = json.jsonify(error='You did not provide an id parameter.', status=404)
				response.status_code = 404
			return response
		if username is None or username == "":
			response = json.jsonify(error='You did not provide a username parameter.', status=404)
			response.status_code = 404
			return response
		results = pic_model.get_photo(pid)
		if results is None:
			response = json.jsonify(error='Invalid id. The picid does not exist.', status=422)
			response.status_code = 422
			return response
		specific_user = main_model.get_specific_user(username)
		if len(specific_user)==0:
			response = json.jsonify(error='Invalid username. The username does not exist.', status=422)
			response.status_code = 422
			return response
		# else:
		# 	if 'username' not in session and session['username'] != username:
		# 		response = json.jsonify(error='Invalid action. The username needs to login.', status=400)
		# 		response.status_code = 400
		# 		return response
		user = favorite_model.user_is_exist(username, pid)
		if len(user) > 0:
			response = json.jsonify(error='The user has already favorited this photo.', status=403)
			response.status_code = 403
			return response
		result = favorite_model.add_favorite(pid, username)
		if result:
			response = json.jsonify(id=pid, status=201)
			response.status_code = 201
			return response
		else:
			return json.jsonify(error="update database error", status=400)
	except Exception as e:
		response = json.jsonify(error="other errors", status=400)
		response.status_code = 400
		return response
