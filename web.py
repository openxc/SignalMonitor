from flask import Flask
from flask import request

app = Flask(__name__)

@app.route('/')
def index():
    return 'Index page'

@app.route('/posty', methods=['POST'])
def posty():
    print("Data")
    print(request.data)
    return "Thanks"

if __name__ == '__main__':
    app.run(host='0.0.0.0', debug=True)
