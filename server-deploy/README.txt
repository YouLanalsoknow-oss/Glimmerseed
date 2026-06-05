
================================================================================
                       Glimmerseed Server Deployment
================================================================================

Step 1: Copy files to server
----------------------------
1. Connect to server via Remote Desktop (8.134.80.158)
2. Enable local drive sharing in Remote Desktop options
3. Copy entire server-deploy folder to C:\ on server
4. Final structure should be:
   C:\server-deploy\
   ├── 1-check-server.bat
   ├── 2-setup-mysql.bat
   ├── 3-build-backend.bat
   ├── 4-configure.bat
   ├── 5-start-server.bat
   ├── 6-test-api.bat
   ├── open-firewall.bat
   ├── README.txt
   ├── init_db.sql
   └── backend\
       ├── pom.xml
       ├── build.bat
       ├── start.bat
       └── ...

Step 2: Run scripts in order
----------------------------
Open CMD on server, navigate to C:\server-deploy\, run:

1-check-server.bat     - Check if Java/Maven/MySQL are installed
2-setup-mysql.bat      - Initialize database
3-build-backend.bat    - Build project
4-configure.bat        - Configure DB password and API Key
5-start-server.bat     - Start server
6-test-api.bat         - Test API

Step 3: Configure Firewall
--------------------------
If needed, open Windows Firewall port 8080:
  open-firewall.bat

Step 4: Configure Alibaba Cloud Security Group
----------------------------------------------
Log into Alibaba Cloud console, open port 8080

================================================================================
