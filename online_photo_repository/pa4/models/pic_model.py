__author__ = 'ray'

import hashlib
import random

from pa4.config import *
from pa4.extensions import mysql


def get_photo(picId):
	cur = mysql.connection.cursor()
	sql_str = "SELECT * FROM photo WHERE picid = '{0}'".format(picId)
	try:
		cur.execute(sql_str)
		return cur.fetchone()
	except Exception as e:
		print e
		return False
	finally:
		cur.close()


def delete_picture_by_id(picid, albumid):
	cur = mysql.connection.cursor()
	cur.execute("DELETE  From contain WHERE `picid` = '{0}' AND `albumid` = '{1}'".format(picid, albumid))
	cur.execute("DELETE  FROM photo WHERE `picid` = '{0}'".format(picid))
	cur.execute("DELETE FROM favorite WHERE `picid` = '{0}'".format(picid))
	mysql.connection.commit()
	return True


def add_photo(filename, albumid, fExt, picid, caption, date):
	url = filename
	cur = mysql.connection.cursor()
	cur.execute(
		"INSERT INTO photo (picid, url, format, date) VALUES ('{0}', 'pictures/{1}','{2}','{3}')".format(
			picid, url, fExt, date))
	# sequence number
	cur.execute("SELECT * FROM contain WHERE `albumid` = '{0}'".format(albumid))
	contains = cur.fetchall()
	seq = 0
	for contain in contains:
		if int(contain[3]) > seq:
			seq = int(contain[3])
	seq += 1
	cur.execute(
		"INSERT INTO contain (albumid, picid, caption, sequencenum) VALUES ({0}, '{1}', '{2}', {3})".format(
			albumid, picid, caption, seq))
	mysql.connection.commit()
	return True


def allowed_file(filename):
	return '.' in filename and filename.rsplit('.', 1)[1] in ALLOWED_EXTENSIONS


def gen_hash(filename, date):
	"""
	function to generate uniq hash id
	:param filename:
	:param date:
	:return:
	"""
	h = hashlib.new('ripemd160')
	h.update(str(random.random() * 10000) + filename + str(date))
	return str(h.hexdigest())


def get_pic_caption(picid):
	cur = mysql.connection.cursor()
	sql_str = "SELECT * from contain WHERE picid ='%s';" % picid
	try:
		cur.execute(sql_str)
		return cur.fetchall()
	except Exception as e:
		print e
		return False
	finally:
		cur.close()


def update_pic_caption(picid, caption):
	res = get_photo(picid)
	if res is None:
		return False;
	cur = mysql.connection.cursor()
	sql_str = "UPDATE contain set caption = '{0}' WHERE picid = '{1}'".format(caption, picid)
	try:
		cur.execute(sql_str)
		mysql.connection.commit()
		return True
	except Exception as e:
		print e
		return False
	finally:
		cur.close()


def get_pic_by_seq(sequencenum):
	cur = mysql.connection.cursor()
	sql_str="SELECT * FROM photo WHERE sequencenum = '{0}'".format(sequencenum)
	try:
		cur.execute(sql_str)
		return cur.fetchone()
	except Exception as e:
		print e
		return False
	finally:
		cur.close()