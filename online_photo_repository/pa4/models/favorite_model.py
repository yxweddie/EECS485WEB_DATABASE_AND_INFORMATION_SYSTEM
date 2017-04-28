__author__ = 'Shiyu'
from pa4.extensions import mysql
from collections import namedtuple
from datetime import datetime

Favorite = namedtuple('Favorite', ['favoriteid', 'picid', 'username', 'date'])


def get_favorites_num_by_picid(picid):
    cur = mysql.connection.cursor()
    query = "SELECT COUNT(*) FROM favorite WHERE picid='%s'" % (picid)
    try:
        cur.execute(query)
        return cur.fetchall()
    except Exception as e:
        print e
        return False
    finally:
        cur.close()


def get_last_favorites_user(picid):
    cur = mysql.connection.cursor()
    query = "SELECT username FROM favorite WHERE picid='%s' ORDER BY date DESC LIMIT 1" % (picid)
    try:
        cur.execute(query)
        return cur.fetchall()
    except Exception as e:
        print e
        return False
    finally:
        cur.close()


def add_favorite(picid, username):
    cur = mysql.connection.cursor()
    query = "INSERT INTO favorite(picid, username, date) VALUES ('{0}','{1}','{2}')".format(picid, username,
                                                                                                        datetime.today().strftime(
                                                                                                            "%Y-%m-%d %H:%M:%S"))
    try:
        cur.execute(query)
        mysql.connection.commit()
        return True
    except Exception as e:
        print e
        return False
    finally:
        cur.close()


def user_is_exist(username, picid):
    cur = mysql.connection.cursor()
    query = "SELECT * FROM favorite WHERE username='{0}' AND picid='{1}'".format(username, picid)
    try:
        cur.execute(query)
        return cur.fetchall()
    except Exception as e:
        print e
        return False
    finally:
        cur.close()
