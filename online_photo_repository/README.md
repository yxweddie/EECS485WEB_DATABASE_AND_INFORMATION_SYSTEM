# Get Going With Python and Flask

## Install Python, PIP, Virtualenv, etc:

If you are doing this on the EECS server machines we have assigned to you, all of this is already installed for you.

- [OSX Users](http://docs.python-guide.org/en/latest/starting/install/osx/)
- [Windows Users](http://docs.python-guide.org/en/latest/starting/install/win/)
- [Linux Users](http://docs.python-guide.org/en/latest/starting/install/linux/)

## Start the Virtualenv and Install Dependencies:

Run `virtualenv venv --distribute` this is going to create a safe place for you to install packages and run your app. Otherwise you need to install a package on your whole machine which can cause conflicts and horrible nightmares.

Then Run `source venv/bin/activate`. This step is important, and mostly magic. You need to run this command **every time you start developing** with a new terminal window. Otherwise your terminal will not be using the virtual environment and things will break. You should see something like `(venv)your-computer:python username$`. (This is slightly different on Windows, see above).

Then run `pip install -r requirements.txt`. This only works correctly because I already gave you a `requirements.txt` file with the right dependencies. If you add a new dependency (like MySQLdb) simple run `pip install mysql-python`.

Unless you're lucky `pip install mysql-python` probably failed. Thats because you need to install `mysql` first. 

- On linux this should fix the problem:  
  `apt-get install build-essential python-dev libmysqlclient-dev`
- On Mac or Windows [read here](http://mysql-python.blogspot.com/2012/11/is-mysqldb-hard-to-install.html).
- On My Mac I ran `brew install  mysql` and then had to run `sudo chown -R $(whoami) /usr/local/` because the link failed. Then I had to run `brew link mysql`. FINALLY `pip install mysql-python` worked just fine. :)

Whew, fun right? Welcome to web development, sometimes setting up tools is a huge pain.

## Run the Darn Thing:
You can simply run `python app.py` now and you'll have an app running!

Once you've gotten your app working fine run `pip freeze > requirements.txt` to save your dependencies as they are. This way when your group mates pull your code they can just run `pip install -r requirements.txt` just like before!
