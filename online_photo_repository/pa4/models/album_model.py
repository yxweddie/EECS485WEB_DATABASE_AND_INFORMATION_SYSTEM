__author__ = 'ray'
import datetime

from pa4.extensions import mysql


def get_album(albumid):
	cur = mysql.connection.cursor()
	cur.execute("SELECT * FROM album WHERE albumid = '{0}'".format(albumid))
	return cur.fetchall()


def get_pic_of_album(albumid):
	cur = mysql.connection.cursor()
	sql_str = "SELECT * From contain as contain INNER Join photo as photo on contain.picid = photo.picid where contain.albumid = {0} ORDER BY contain.sequencenum".format(albumid)
	cur.execute(sql_str)
	return cur.fetchall()


def add_album(albumName, username):
	cur = mysql.connection.cursor()
	cur.execute("INSERT INTO album (title, created, lastupdated, username) VALUES ("
				"'{0}','{1}','{2}','{3}')".format(albumName, datetime.date.today(), datetime.date.today(), username))
	mysql.connection.insert_id()
	mysql.connection.commit()
	return True


def delete_album_by_id(albumid):
	cur = mysql.connection.cursor()
	cur.execute("DELETE  FROM album WHERE `albumid` = '{0}'".format(albumid))
	mysql.connection.commit()
	return True


def get_access_album_user(albumid):
	cur = mysql.connection.cursor()
	sql_str = "SELECT * FROM albumaccess WHERE `albumid` = '{0}'".format(albumid)
	cur.execute(sql_str)
	return cur.fetchall()


def update_album_info(albumid, name, status):
	cur = mysql.connection.cursor()
	sql_str = "UPDATE album set title = '{0}', access = '{1}' WHERE albumid = {2}".format(name, status, albumid)
	try:
		cur.execute(sql_str)
		mysql.connection.commit()
		if status == 'public':
			sql_str2 = "Delete from albumaccess where albumid={0}".format(albumid)
			cur.execute(sql_str2)
			mysql.connection.commit()
		return True
	except Exception as e:
		print e
		return False
	finally:
		cur.close()


def remove_user_privilege(albumid, username):
	cur = mysql.connection.cursor()
	sql_str = "DELETE FROM albumaccess WHERE albumid = {0} AND username = '{1}'".format(albumid, username)
	try:
		cur.execute(sql_str)
		mysql.connection.commit()
		return True
	except Exception as e:
		print e
		return False
	finally:
		cur.close()


def is_has_privilege(albumid, username):
	cur = mysql.connection.cursor()
	sql_str = "SELECT COUNT(*) FROM albumaccess WHERE albumid = {0} AND username = '{1}'".format(albumid, username)
	try:
		cur.execute(sql_str)
		length = len(cur.fetchall())
		if length == 1:
			return False
		return True
	except Exception as e:
		print e
		return False
	finally:
		cur.close()


def add_user_privilege(albumid, username):
	cur = mysql.connection.cursor()
	sql_str = "INSERT INTO albumaccess VALUES({0}, '{1}')".format(albumid, username)
	try:
		cur.execute(sql_str)
		mysql.connection.commit()
		return True
	except Exception as e:
		print e
		return False
	finally:
		cur.close()


def select_user_from_albumaccess(albumid, username):
	cur = mysql.connection.cursor()
	sql_str = "SELECT COUNT(*) FROM albumaccess WHERE albumid={0} AND username='{1}'".format(albumid, username)
	try:
		cur.execute(sql_str)
		return cur.fetchall()
	except Exception as e:
		print e
		return False
	finally:
		cur.close()
