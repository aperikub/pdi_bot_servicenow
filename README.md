to run, the code need the config/login.properties file (here renamed login_example.properties) with :
-servicenow login
-servicenow password
-pdi user login
-pdi user password
-authenticator app key*

*you need to activate the 2FA using authenticator app (I use google authenticator) you will be provided a QR code, you need to :
1. scan the qr code with a standard reader and save the text (your key is in it) it will be like : otpauth://totp/ssosignon.servicenow.com:example%40gmail.com?secret=J5PVIZWNJTUUEGWN&issuer=ssosignon.servicenow.com, the key here is J5PVIZWNJTUUEGWN 
2. scan the qr code with your authenticator app AND activate it in servicenow by entering the code (if you take too long, the qr code will expire)
