1. Go to Server Folder and run the Server
	java -jar NewServer.jar
2. Go to Client Folder and run the Client.
	java -jar NewDaemon.jar
3. Every folder must contain config.txt
	In the config.txt
	LISTEN_PORT=9123
4. SEND node.conf
	node.conf is sent to the Server and saved as "received_file.txt"
5. SENDT node.conf
	The last 500 lines of node.conf is also sent to the Server and saved as "received_file.txt"
6. START xyz.jar
	The xyz.jar is launched in new window.(xyz.jar must be contained in the same directory as NewServer.jar)
7. STOP xyz.jar
	The process xyz.jar stops.
8. PS
	The process data is sent and saved as "received_process.txt"
9. UPDATE node.conf
	The server sends file "file_to_send.txt" to the client, and saved as node.conf.
10. LS myapp
	The client sends the directory data of myapp to the server and saved as "received_subfolder_data.txt"