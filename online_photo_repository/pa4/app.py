from flask import Flask, app, session
from datetime import timedelta

import controllers
from pa4.config import *
from pa4.extensions import mysql

app = Flask(__name__, template_folder='views')

# initiate the db
app.config['MYSQL_USER'] = 'group126'
app.config['MYSQL_PASSWORD'] = 'eecs485126'
app.config['UPLOAD_FOLDER'] = PicDir
app.config['MYSQL_DB'] = 'group126'
mysql.init_app(app)

app.secret_key = 'v4y9q6ko6r'

# register_blueprint
app.register_blueprint(controllers.main_blue, url_prefix=Home_prefix)
app.register_blueprint(controllers.album_blue, url_prefix=Home_prefix)
app.register_blueprint(controllers.albums_blue, url_prefix=Home_prefix)
app.register_blueprint(controllers.pic_blue, url_prefix=Home_prefix+'/pic')
app.register_blueprint(controllers.user_blue, url_prefix=Home_prefix)
app.register_blueprint(controllers.search_blue, url_prefix=Home_prefix)


@app.before_request
def init_session():
	session.permanent = True
	app.permanent_session_lifetime = timedelta(minutes=5)


def main():
	"""
	comment this out using a WSGI like gunicorn
	if you dont, gunicorn will ignore it anyway
	"""
	app.secret_key = 'v4y9q6ko6r'
	# PicDir = '/var/www/html/group126/project/python/pa1/static/pictures'
	# print app.config['UPLOAD_FOLDER']
	app.run(host="0.0.0.0", port=5926, debug=True)
	#app.run(debug=True)


if __name__ == '__main__':
	main()


