# coding=utf8
# -*- coding: utf8 -*-
# vim: set fileencoding=utf8 :
import hashlib
import itertools
import os

from flask import *
from pa4.models import *
from pa4.config import *

user_blue = Blueprint('user', __name__, template_folder='views')


@user_blue.route('/user', methods=['GET', 'POST'])
def user():
	if user_model.is_login():
		# this can also direct to the albums?
		return redirect(url_for('.user_edit'))
	error = ''
	if request.method == 'GET':
		return render_template('signUp.html', Error=error)
	else:
		username = request.form['username']
		password1 = request.form['password1']
		password2 = request.form['password2']
		first_name = request.form['firstname']
		last_name = request.form['lastname']
		email = request.form['email']

		# check if there is exsting username
		check_if_has_this_user = main_model.get_specific_user(username)
		if len(check_if_has_this_user) == 1:
			error = 'username exist, please enter a new one!'
			return render_template('signUp.html', Error=error)
		# check if the password is the same
		if password1 != password2:
			error = 'password does not match'
			return render_template('signUp.html', Error=error)
		password = hash_password(password1)
		user_model.create_account(username, password, first_name, last_name, email)
		if user_model.validate_login(username, password):
			session['login'] = True
			session['username'] = username
			return redirect(url_for('main.index'))
		else:
			session['login'] = None
			error = 'SignUp, but not Log in'
			return render_template('signUp.html', Error=error)


@user_blue.route('/user/edit', methods=['GET', 'POST'])
def user_edit():
	if user_model.is_login():
		error = ''
		username = session['username']
		if request.method == 'POST':
			current_password = request.form['cpassword']
			current_password = hash_password(current_password)
			if not user_model.validate_login(username, current_password):
				error = 'Wrong password'
			else:
				new_password1 = request.form['password1']
				new_password2 = request.form['password2']
				if new_password1 != new_password2:
					error = 'new password does not match'
				else:
					first_name = request.form['firstname']
					last_name = request.form['lastname']
					email = request.form['email']
					password = hash_password(new_password1)
					user_model.update_account(username, password, first_name, last_name, email)
					return redirect(url_for('main.index'))
			return render_template('editAccount.html', Error=error, username=username)
		else:
			return render_template('editAccount.html', Error=error, username=username)
	else:
		return redirect(url_for('.login', return_url=request.path))


@user_blue.route('/user/delete')
def delete_account():
	if user_model.is_login():
		username = session['username']
		if username != '':
			# albums = list(itertools.chain(*albums_model.get_albums(username)))
			albums = albums_model.get_albums(username)
			for album in albums:
				album = list(itertools.chain(album))
				album_id = str(album[0])
				pictures = album_model.get_pic_of_album(int(album_id))
				for pic in pictures:
					pic_model.delete_picture_by_id(pic[0], album_id)
					try:
						os.remove("static/{0}".format(pic[5]))
					except:
						return False
				user_model.delete_access(username, album_id)
				album_model.delete_album_by_id(album_id)
			user_model.delete_user(username)
			return logout()
		else:
			print 'no username'
	else:
		return redirect(url_for('.login', return_url='.user_edit'))


@user_blue.route('/logout')
def logout():
	session.pop('username', None)
	session.pop('login', None)
	return redirect(url_for('main.index'))


@user_blue.route('/user/login', methods=['GET', 'POST'])
def login():
	if user_model.is_login():
		return redirect(url_for('main.index'))
	page_title = "Log In"
	return_url = "/"
	error = ''
	if request.method == 'GET':
		if request.args.get('return_url', '') == '':
			print "no return_url"
		else:
			return_url = request.args.get('return_url', '')
	else:
		return_url = request.form['returnUrl']
		username = request.form['username']
		password = request.form['password']
		password = hash_password(password)
		user_check = main_model.get_specific_user(username);
		if len(user_check) == 1:
			if user_model.validate_login(username, password):
				session['login'] = True
				session['username'] = username
				return redirect(return_url)
			else:
				session['login'] = None
				error = 'Invalid password'
		else:
			print user_check
			session['login'] = None
			error = 'Invalid username'
	return render_template('login.html', pageTitle=page_title, returnUrl=return_url, error=error)


def hash_password(password):
	h = hashlib.new('ripemd160')
	h.update(password)
	return str(h.hexdigest())
