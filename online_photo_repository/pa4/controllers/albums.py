__author__ = 'Yixiang
import album
import itertools
import main
import os

from main import *
from pa4.models import *

albums_blue = Blueprint('albums', __name__, template_folder='views')


@albums_blue.route('/albums')
def albums():
	if user_model.is_login():
		return specific_albums()
	else:
		return all_albums()


def all_albums():
	albums = albums_model.get_all_albums()
	return render_template('albums.html', albums=albums)


# login
def specific_albums():
	options = {
		"edit": False
	}
	selected_username = session["username"]
	# TODO: check if the user selected
	if selected_username == '':
		missing = "you miss pick a specific user o view the albums"
		return render_template('404.html', missing=missing)

	user = list(itertools.chain(*main_model.get_specific_user(selected_username)))
	albumsName = albums_model.get_albums(selected_username)

	# TODO: check if the user exit

	return render_template('albums.html', pageTitle='{0} {1}\'s albums'.format(user[2], user[3]),
						   albumsName=albumsName, user=user[0], **options)


@albums_blue.route('/albums/edit/', methods=['GET', 'POST'])
def edit_albums():
	if user_model.is_login():
		options = {
			"edit": True
		}
		if request.method == 'GET':
			selected_username = session["username"]
			if selected_username == '':
				missing = "you miss pick a specific user to Edit the albums"
				return render_template('404.html', missing=missing)
			user = list(itertools.chain(*main_model.get_specific_user(selected_username)))
			albums_name = albums_model.get_albums(selected_username)
			return render_template('albums.html', pageTitle='Editing {0} {1}\'s albums'.format(user[2], user[3]),
								   albumsName=albums_name, user=user[0], **options)
		else:
			op = request.form['op']
			username = session["username"]
			if op == 'delete':
				albumid = request.form['albumid']
				pictures = album_model.get_pic_of_album(albumid)
				for pic in pictures:
					try:
						pic_model.delete_picture_by_id(pic[0], albumid)
						os.remove("static/{0}".format(pic[5]))
					except Exception as e:
						print e
				album_model.delete_album_by_id(albumid)
				# flash('Delete album Success', 'flash-success')
			else:
				albumName = request.form['albumName']
				album_model.add_album(albumName, username)
		# flash('Add album Success', 'flash-success')
		return redirect(url_for('.edit_albums', username=username))
	else:
		return redirect(url_for('user.login', return_url=request.path))


"""

    functions

"""
