__author__ = 'Shiyu'
from collections import defaultdict
from main_model import *

def get_albums(username):
    """
    fuction find the albums by the specific user
    :param username:
    :return:
    """
    cur = mysql.connection.cursor()
    cur.execute("SELECT * FROM album WHERE username = '{0}'".format(username))
    return cur.fetchall()


def get_all_albums():
    cur = mysql.connection.cursor()
    cur.execute("SELECT * FROM album WHERE access='public'")
    return cur.fetchall()


def get_other_albums(username):
    cur=mysql.connection.cursor()
    cur.execute("SELECT a.albumid, a.title, a.username, a.access "
                "FROM user u, albumaccess ac, album a "
                "WHERE u.username=ac.username AND ac.albumid=a.albumid and u.username='{0}' "
                "UNION "
                "SELECT albumid, title, username, access FROM album WHERE access='public' and username <> '{0}' ".format(username))
    result = defaultdict(list)
    for row in cur.fetchall():
        result[row[2]].append((row[0], row[1], row[3]))
    return dict(result)
