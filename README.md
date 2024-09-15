
To run, the bot need the config/login.properties file (here renamed login_example.properties) with :
-servicenow login [developer_login]
-servicenow password [developer_password]
-pdi instance user login [instance_login]
-pdi user password [instance_password]
-authenticator app key* [totp_key]

*you need to activate the 2FA using authenticator app (I use google authenticator), you need to :

1. go to https://signon.service-now.com/sso?id=sso_settings and select Authenticator App, if already set you need to disable it

2. you will be provided a QR code

3. scan the qr code with a standard reader and save the text (your totp key is in it) it will be like : otpauth://totp/ssosignon.servicenow.com:example%40gmail.com?secret=J5PVIZWNJTUUEGWN&issuer=ssosignon.servicenow.com, the totp key here is J5PVIZWNJTUUEGWN

4. scan the qr code with Google Authenticator : Click the "+" sign and select the qr code option (add the qr code to the app AND activate it in servicenow by entering the code in the page showing the qr code (if you take too long, the qr code will expire and you will have to start again)
