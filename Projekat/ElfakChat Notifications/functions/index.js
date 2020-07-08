'use strict'
const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);


exports.sendNotification = functions.database.ref('/Friend Request Notifications/{receiver_user_id}/{notification_id}')
.onWrite((data, context) =>
{
	const receiver_user_id = context.params.receiver_user_id;
	const notification_id = context.params.notification_id;


	console.log( receiver_user_id);


	if (!data.after.val()) 
	{
		console.log('A notification has been deleted :' , notification_id);
		return null;
	}

	const  sender_user_id = admin.database().ref(`/Friend Request Notifications/${receiver_user_id}/${notification_id}`).once('value');
	return sender_user_id.then(fromUserResult=>{

		const send_user_id = fromUserResult.val().from;

		const Query = admin.database().ref(`/Users/${send_user_id}/name`).once('value');

		return Query.then(userResult =>{

			const userName = userResult.val();

			const Token = admin.database().ref(`/Users/${receiver_user_id}/device_token`).once('value');    

	 return Token.then(result => 
	 {
		const token_id = result.val();

		const payload = 
		{		
						
			data:
			{									
				title:"New Friend Request",			
				text: `${userName} sent you Friend Request.`,		
				sound: "default",
				icon:"default",
				
			}
		};

		return admin.messaging().sendToDevice(token_id, payload)
		.then(response => {
				console.log('Notification sent.');
			});
		});


		
	   });

		
	});

	
});