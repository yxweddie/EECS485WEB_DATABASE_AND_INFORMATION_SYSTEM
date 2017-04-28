from flask import *
from pa4.config import *
from pa4.models import *

main_blue = Blueprint('main', __name__, template_folder='views')


@main_blue.route('/index.html')
@main_blue.route('/')
def index():
	if user_model.is_login():
		return login_index()
	else:
		return not_login_index()


# not login
def not_login_index():
	return render_template("index.html", is_login=False)


# login
def login_index():
	username = session["username"]
	other_albums = albums_model.get_other_albums(username)
	own_albums = albums_model.get_albums(username)
	return render_template("index.html", is_login=True, other_albums=other_albums, own_albums=own_albums,
						   username=username)


@main_blue.route('/resources/<path:filename>')
def flickr_images(filename):
	return send_from_directory('../resources/', filename, as_attachment=True)