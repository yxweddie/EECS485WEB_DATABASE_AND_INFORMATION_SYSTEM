__author__ = 'Shiyu'
from pa4.extensions import mysql


def get_users():
    """
    function to find the users from the db
    :return:
    """
    cur = mysql.connection.cursor()
    cur.execute('''SELECT username FROM user''')
    msgs = cur.fetchall()
    return msgs


def get_other_users(albumid, username):
    """
    function to find the users from the db
    :return:
    """
    cur = mysql.connection.cursor()
    sql_str = "SELECT username FROM user WHERE username NOT IN " \
              "(SELECT username FROM albumaccess WHERE albumid = {0}) AND username <> '{1}'" \
              "".format(albumid, username)
    print sql_str
    cur.execute(sql_str)
    msgs = cur.fetchall()
    return msgs


def get_specific_user(username):
    """
    function find the specific user
    :param username:
    :return:
    """
    cur = mysql.connection.cursor()
    cur.execute("SELECT * FROM user WHERE username = '{0}'".format(username))
    return cur.fetchall()


def get_other_albums(username):
    cur=mysql.connection.cursor()
    cur.execute("SELECT a.albumid, a.title, a.username, a.access "
                "FROM user u, albumaccess ac, album a "
                "WHERE u.username=ac.username AND ac.albumid=a.albumid and u.username='{0}' "
                "UNION "
                "SELECT albumid, title, username, access FROM album WHERE access='public'".format(username))
    return cur.fetchall()
