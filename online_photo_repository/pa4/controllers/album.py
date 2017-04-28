import datetime
import itertools
import os

from flask import *
from pa4.config import *
from pa4.models import *
from werkzeug import secure_filename

album_blue = Blueprint('album', __name__, template_folder='views')


@album_blue.route('/album/', defaults={'album_id': -1})
@album_blue.route('/album/<int:album_id>')
def album(album_id):
	options = {
		"edit": False
	}
	if album_id == -1:
		missing = "you miss pick a specific album_id to view the album"
		return render_template('404.html', missing=missing)

	album_info = list(itertools.chain(*album_model.get_album(album_id)))
	user = list(itertools.chain(*main_model.get_specific_user(album_info[4])))
	pictures = album_model.get_pic_of_album(album_id)
	if user_model.is_login():
		login_user = session['username']
	else:
		login_user = ''
	valid_result = list(itertools.chain(*album_model.select_user_from_albumaccess(album_id, login_user)))[0]
	# full:show all, part:show pic only, redirect:redirect to login
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
		return render_template('album.html', pageTitle='{0} {1}\'s {2} album'.format(user[2], user[3], album_info[1]),
						   pictures=pictures, album=album_info, user=user[0], check_user=status, **options)


@album_blue.route('/album/edit/', defaults={'album_id': -1})
@album_blue.route('/album/edit/<int:album_id>', methods=['GET', 'POST'])
def edit_album(album_id):
	options = {
		"edit": True
	}

	if request.method == 'GET':
		if album_id == -1:
			missing = "you miss pick a specific album_id to edit the album"
			return render_template('404.html', missing=missing)
		album = list(itertools.chain(*album_model.get_album(album_id)))
		user = list(itertools.chain(*main_model.get_specific_user(album[4])))
		users = list(itertools.chain(*main_model.get_other_users(album_id, user[0])))
		pictures = album_model.get_pic_of_album(album_id)
		access_user = album_model.get_access_album_user(album_id)
		return render_template('album.html', pageTitle='{0} {1}\'s {2} album'.format(user[2], user[3], album[1]),
							   pictures=pictures, album=album, user=user[0], users=users, access_user=access_user,
							   url_prefix=Home_prefix, **options)
	else:
		op = request.form['op']
		username = request.form['username']
		album_id = request.form['albumid']
		if op == 'delete':
			picid = request.form["picid"]
			photo = pic_model.get_photo(picid)
			try:
				os.remove("static/{0}".format(photo[1]))
			except Exception as e:
				print e
			pic_model.delete_picture_by_id(picid, str(album_id))
		else:
			caption = request.form['caption']
			file = request.files['picName']
			print 'print os.path is: ', os.path.join(PicDir)
			if file and pic_model.allowed_file(file.filename):
				filename = secure_filename(file.filename)
				date = datetime.date.today()
				picid = pic_model.gen_hash(filename, date)
				fExt = filename.rsplit('.', 1)[1]
				url = picid + '.' + fExt
				filename = secure_filename(url)
				file.save(os.path.join(PicDir, filename))
				pic_model.add_photo(filename, album_id, fExt, picid, caption, date)
		return redirect(url_for('album.edit_album', album_id=album_id))


@album_blue.route('/album/edit/info', methods=['GET', 'POST'])
def modify_albume_info():
	album_name = request.json['name']
	album_status = request.json['status']
	album_id = request.json['album_id']
	print album_name
	result = album_model.update_album_info(album_id, album_name, album_status)
	return str(result)


@album_blue.route('/album/edit/remove_access', methods=['GET', 'POST'])
def remove_user_privilege():
	album_id = request.json['album_id']
	username = request.json['username']
	result = album_model.remove_user_privilege(album_id, username)
	return str(result)


@album_blue.route('/album/edit/add_access', methods=['GET', 'POST'])
def add_user_privilege():
	album_id = request.json['album_id']
	username = request.json['username']
	exist = album_model.is_has_privilege(album_id, username)
	if exist:
		return 'False'
	result = album_model.add_user_privilege(album_id, username)
	return str(result)
