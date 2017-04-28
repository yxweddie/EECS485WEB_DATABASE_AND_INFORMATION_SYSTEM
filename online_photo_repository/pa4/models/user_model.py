from flask import *
from pa4.extensions import mysql


def delete_access(username, albumid):
	cur = mysql.connection.cursor()
	cur.execute(
		"DELETE FROM albumaccess WHERE username = '{0}' AND albumid = '{1}'".format(username, albumid))
	mysql.connection.commit()


def delete_user(username):
	cur = mysql.connection.cursor()
	cur.execute("DELETE FROM user WHERE username = '{0}'".format(username))
	mysql.connection.commit()


def update_account(username, password, firstname, lastname, email):
	password = password[0:19]
	cur = mysql.connection.cursor()
	cur.execute(
		"UPDATE user set `password` = '{0}', firstname ='{1}', lastname = '{2}', email = '{3}' WHERE username = '{4}'".format(
			password, firstname, lastname, email, username))
	mysql.connection.commit()


def create_account(username, password, firstname, lastname, email):
	password = password[0:19]
	cur = mysql.connection.cursor()
	cur.execute(
		"INSERT INTO user (username, password, firstname, lastname, email) VALUES ('{0}','{1}','{2}','{3}','{4}')".format(
			username, password, firstname, lastname, email))
	mysql.connection.commit()


def validate_login(username, password):
	password = password[0:19]
	print(password)
	cur = mysql.connection.cursor()
	cur.execute(
		"SELECT * FROM user WHERE username  = '{0}' AND password = '{1}'".format(username, password))
	res = cur.fetchall()
	return len(res) == 1


def is_login():
	if 'login' in session:
		if session['login'] == True:
			return True
		else:
			return False
