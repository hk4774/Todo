from datetime import datetime

import sys
from flask import Flask, request, g, jsonify
from flask_login import LoginManager
from flask_login import login_user, logout_user, current_user, login_required
from flask_sqlalchemy import SQLAlchemy
from itsdangerous import URLSafeTimedSerializer
from werkzeug.security import generate_password_hash, check_password_hash
import logging

app = Flask(__name__)
app.config.from_pyfile('todoapp.cfg')
app.logger.addHandler(logging.StreamHandler(sys.stdout))
app.logger.setLevel(logging.ERROR)
db = SQLAlchemy(app)

login_manager = LoginManager()
login_manager.init_app(app)
login_serializer = URLSafeTimedSerializer(app.secret_key)


class User(db.Model):
    __tablename__ = "users"
    id = db.Column('user_id', db.Integer, primary_key=True)
    username = db.Column('username', db.String(20), unique=True, index=True)
    password = db.Column('password', db.String(250))
    registered_on = db.Column('registered_on', db.DateTime)
    todos = db.relationship('Todo', backref='user', lazy='dynamic')

    def __init__(self, username, password):
        self.username = username
        self.set_password(password)
        self.registered_on = datetime.utcnow()

    def set_password(self, password):
        self.password = generate_password_hash(password)

    def check_password(self, password):
        return check_password_hash(self.password, password)

    def is_authenticated(self):
        return True

    def is_active(self):
        return True

    def is_anonymous(self):
        return False

    def get_id(self):
        return unicode(self.id)

    def __repr__(self):
        return '<User %r>' % (self.username)


class Todo(db.Model):
    __tablename__ = 'todos'
    id = db.Column('todo_id', db.Integer, primary_key=True)
    title = db.Column(db.String(60))
    done = db.Column(db.Boolean)
    pub_date = db.Column(db.DateTime)
    user_id = db.Column(db.Integer, db.ForeignKey('users.user_id'))

    def __init__(self, title):
        self.title = title
        self.done = False
        self.pub_date = datetime.utcnow()


class Todo_Items(db.Model):
    __tablename__ = 'todos_items'
    id = db.Column('todo_items_id', db.Integer, primary_key=True)
    text = db.Column(db.String)
    done = db.Column(db.Boolean)
    parent_id = db.Column(db.Integer, db.ForeignKey('todos.todo_id'))

    def __init__(self, text, parent_id):
        self.parent_id = parent_id
        self.text = text
        self.done = False
        self.pub_date = datetime.utcnow()


db.create_all()


@app.route('/todos/new', methods=['GET', 'POST'])
def new():
    if request.method == 'POST':
        if not request.headers['title']:
            return jsonify({'status': 0, 'message': 'Title is required'})
        else:
            todo = Todo(request.headers['title'])
            todo.user = g.user
            db.session.add(todo)
            db.session.commit()
            return jsonify({'status': 1, 'message': 'Todo item was successfully created'})
    else:
        return jsonify({'status': 0, 'message': 'Something went wrong'})


@app.route('/todos/new/<int:todo_id>', methods=['GET', 'POST'])
def new_item(todo_id):
    if request.method == 'POST':
        todo_item = Todo_Items(request.headers['text'], todo_id)
        db.session.add(todo_item)
        db.session.commit()
        return jsonify({'status': 1, 'message': 'Todo item was successfully created'})
    else:
        return jsonify({'status': 0, 'message': 'Something went wrong'})


@app.route('/todos/delete/<int:todo_id>', methods=['GET', 'POST'])
@login_required
def delete(todo_id):
    if not current_user.is_authenticated():
        return jsonify({'status': 0, 'message': 'You are not authorized to edit this todo item'})
    if request.method == 'POST':
        item = Todo.query.filter_by(id=todo_id).first()
        if item is not None:
            Todo.query.filter_by(id=todo_id).delete()
            Todo_Items.query.filter_by(parent_id=todo_id).delete()
            db.session.commit()
            return jsonify({'status': 1, 'message': 'Todo item was successfully deleted'})
        else:
            return jsonify({'status': 1, 'message': 'Todo item was not found'})


@app.route('/todos/delete/item/<int:todo_id>', methods=['GET', 'POST'])
@login_required
def delete_item(todo_id):
    if not current_user.is_authenticated():
        return jsonify({'status': 0, 'message': 'You are not authorized to edit this todo item'})
    if request.method == 'POST':
        Todo_Items.query.filter_by(id=todo_id).delete()
        db.session.commit()
        return jsonify({'status': 1, 'message': 'Todo item was successfully deleted'})


@app.route('/todos/<int:todo_id>', methods=['GET'])
@login_required
def show(todo_id):
    if not current_user.is_authenticated():
        return jsonify({'status': 0, 'message': 'You are not authorized to edit this todo item'})
    todo_items = Todo_Items.query.filter_by(parent_id=todo_id).all()
    todo_list = []
    for item in todo_items:
        task = {
            'id': item.id,
            'text': item.text,
            'done': item.done
        }
        todo_list.append(task)
    return jsonify({'task': todo_list})


@app.route('/todos', methods=['GET'])
@login_required
def show_all():
    if not current_user.is_authenticated():
        return jsonify({'status': 0, 'message': 'You are not authorized to edit this todo item'})
    todo_item = Todo.query.filter_by(user_id=g.user.id).order_by(Todo.pub_date.desc()).all()
    todo_list = []
    for item in todo_item:
        task = {
            'id': item.id,
            'title': item.title,
            'done': item.done
        }
        todo_list.append(task)
    return jsonify({'task': todo_list})


@app.route('/todos/update/<int:todo_id>', methods=['GET', 'POST'])
@login_required
def update(todo_id):
    todo_item = Todo.query.get(todo_id)
    if todo_item.user.id == g.user.id:
        todo_item.title = request.headers['title']
        todo_item.done = request.headers['done']
        db.session.commit()
        return jsonify({'status': 1, 'message': 'Todo item was successfully updated'})
    return jsonify({'status': 0, 'message': 'You are not authorized to edit this todo item'})


@app.route('/todos/update/item/<int:todo_id>', methods=['GET', 'POST'])
@login_required
def update_item(todo_id):
    todo_item = Todo_Items.query.get(todo_id)
    if todo_item.user.id == g.user.id:
        todo_item.text = request.headers['title']
        todo_item.done = request.headers['done']
        db.session.commit()
        return jsonify({'status': 1, 'message': 'Todo item was successfully updated'})
    return jsonify({'status': 0, 'message': 'You are not authorized to edit this todo item'})


@app.route('/todos/register', methods=['GET', 'POST'])
def register():
    registered_user = User.query.filter_by(username=request.headers['username']).first()
    if registered_user is not None:
        return jsonify({'status': 0, 'message': 'Username is already exists'})
    user = User(request.headers['username'], request.headers['password'])
    db.session.add(user)
    db.session.commit()
    return jsonify({'status': 1, 'message': 'User successfully registered'})


@app.route('/todos/login', methods=['GET', 'POST'])
def login():
    username = request.headers['username']
    password = request.headers['password']
    registered_user = User.query.filter_by(username=username).first()
    if registered_user is None:
        return jsonify({'status': 0, 'message': 'Username is invalid'})
    if not registered_user.check_password(password):
        return jsonify({'status': 0, 'message': 'Password is invalid'})
    if login_user(registered_user):
        return jsonify({'status': 1, 'message': 'Logged in successfully'})
    else:
        return jsonify({'status': 0, 'message': 'Something went wrong'})


@app.route('/todos/logout', methods=['GET', 'POST'])
def logout():
    if current_user.is_authenticated:
        if logout_user():
            return jsonify({'status': 1, 'message': 'Logged out successfully'})
    else:
        return jsonify({'status': 2, 'message': 'Already Logged out'})


@login_manager.user_loader
def load_user(id):
    return User.query.get(id)


@app.before_request
def before_request():
    g.user = current_user
