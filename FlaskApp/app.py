from flask import Flask, jsonify, g, request
import mydb
import math
import time

app = Flask(__name__)

#Establish connection with database
@app.before_request
def before_request():
    g.mydb = mydb.MyDataBase()
    return

#Retrive the list of chatrooms
@app.route("/api/asgn3/get_chatrooms")
def get_chatrooms():    
    query = "SELECT * FROM chatrooms ORDER BY id ASC"
    g.mydb.cursor.execute(query)
    chatroom_list = g.mydb.cursor.fetchall()
    return jsonify(data=chatroom_list,status="OK")

#Retrive the list of messages
@app.route("/api/asgn3/get_messages", methods=['GET'])
def get_messages():
    chatroom_id = request.args.get("chatroom_id", 0, type=int) 
    page = request.args.get("page", 1, type=int)
    query = "select message,name,timestamp,user_id from messages where chatroom_id=%s order by id desc" % (chatroom_id)
    g.mydb.cursor.execute(query)
    
    #Store all the messages return from database
    messages_list = []
    #Store the messages for client
    messages_callback_list = []
    #Mark down the number of message
    count = 0

    while(1):
        #Fetch the message one by one
        message = g.mydb.cursor.fetchone()
        if message == None:
            break
        else:
            #Add the number
            count+=1
            messages_list.append(message)

    #Calculate the number of total page. Each page only contains no more than 10 messages.
    total_page = math.ceil(float(count)/float(10))
    if total_page == 0:
        #At least 1 page
        total_page = 1

    #Append the messages in the specify page
    if page != total_page:
        for i in range((page-1)*10,page*10):
            messages_callback_list.append(messages_list[i])
    else:
        if count != 0:
            for i in range((page-1)*10,count):
                messages_callback_list.append(messages_list[i])
        
    return jsonify(data=messages_callback_list,page = page,status="OK",total_pages=total_page)

#Insert new records to the database
@app.route('/api/asgn3/send_message', methods=['POST'])
def send_message():
    message = None
    name = None
    chatroom_id = None
    user_id = None
    try:
        message = request.form["message"]
        name = request.form["name"]
        chatroom_id = request.form["chatroom_id"]
        user_id = request.form["user_id"]
    finally:
        if message == None or chatroom_id == None or name == None or user_id == None :
            return jsonify(status="ERROR", message="missing parameters")
        else :          
            insert_query = "INSERT INTO messages (chatroom_id,user_id,name,message,timestamp) VALUES (%s,%s,%s,%s,%s)"

            params = (chatroom_id,user_id,name,message,time.strftime("%Y-%m-%d %H:%M:%S", time.localtime(time.time()+8*3600)))
            g.mydb.cursor.execute(insert_query,params)
            g.mydb.db.commit()

            return jsonify(status="OK")

#Close the connection with database
@app.teardown_request
def teardown_request(exception): 
    mydb = getattr(g, 'mydb', None) 
    if mydb is not None:
        mydb.db.close()
    return
    
if __name__ == "__main__":
    app.run(debug=True, host='0.0.0.0')


