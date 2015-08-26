//
//  EmailFunctions.swift
//  SSP Service Tracker
//
//  Created by Connor Hargus on 7/23/15.
//  Copyright (c) 2015 AppMaker. All rights reserved.
//

import Foundation
import UIKit
import CoreData

public class EmailFunctionsBefore {
    
    //Sends an email using the SKPSMTPMessage Framework in "eMail Framework" Folder
    class func sendEmail(sendTo:String, content:String) -> Bool {
        
        var subject = EmailFunctions.formatSubject(storedEmail!)
        
        var parts: NSDictionary = [
            "kSKPSMTPPartContentTypeKey": "text/plain; charset=UTF-8",
            "kSKPSMTPPartMessageKey": content
        ]
        /*
        var mail = SKPSMTPMessage()
        mail.fromEmail = dbscEmail
        mail.requiresAuth = true
        mail.login = dbscEmail
        mail.pass = dbscEmailPass
        
        mail.subject = subject
        mail.wantsSecure = true
        mail.relayHost = "smtp.gmail.com"
        
        mail.relayPorts = [587]
        mail.parts = [parts]
        mail.toEmail = sendTo
        
        //Sends email as a synchronous task
        var sentBool:Bool = false
        dispatch_sync(dispatch_get_global_queue(
            Int(QOS_CLASS_USER_INITIATED.value), 0)) {
                
                if mail.send() == true {
                    sentBool = true
                }
        }
        return sentBool
        */
        return true
    }
    
    class func updateGlobalVariables(email:NSManagedObject?) {
        
        //Use core data to get user and user email address
        let persistentName: String = (NSUserDefaults.standardUserDefaults().objectForKey("userName") as? String) ?? ""
        
        let persistentEmail: String = (NSUserDefaults.standardUserDefaults().objectForKey("userEmail") as? String) ?? ""
        
        client = email?.valueForKey("client") as? String ?? ""
        clientEmail = email?.valueForKey("clientEmail") as? String ?? ""
        clientCode = email?.valueForKey("clientCode") as? String ?? "No ID Card Scanned"
        user = email?.valueForKey("user") as? String ?? persistentName
        userEmail = email?.valueForKey("userEmail") as? String ?? persistentEmail
        dateString = email?.valueForKey("dateString") as? String ?? ""
        startTime = email?.valueForKey("startTime") as? String ?? ""
        endTime = email?.valueForKey("endTime") as? String ?? "Unknown"
        timeTotal = email?.valueForKey("timeTotal") as? String ?? "Unknown"
        checkedService = email?.valueForKey("checkedService") as? String ?? ""
        checkedServiceAbbr = email?.valueForKey("checkedServiceAbbr") as? String ?? ""
        storedEmail = email ?? nil
    }
    
    class func formatComments() -> String {
        
        if comments.count == 0 {
            return ""
        } else {
            var commentsPlusDates: String = ""
            for (var i = 0; i < comments.count; i++) {
                commentsPlusDates += "\n- \(commentTimes[i]): \"\(comments[i])\""
            }
            return "Comments:" + commentsPlusDates + "\n\n"
        }
        
    }
    
    //Formats the email subject line
    class func formatSubject(email:NSManagedObject) -> String {
        
        var tags = ""
        
        if clientCode == "No ID Card Scanned" && endTime == "Unknown" {
            
            tags = "NO ID USED/UNFINISHED: "
        } else if clientCode == "No ID Card Scanned" {
            
            tags = "NO ID USED: "
            
        } else if endTime == "Unknown" {
            tags = "UNFINISHED: "
        }
        
        return "\(tags)\(user) served \(client) as a \(checkedServiceAbbr) on \(dateString)"
        
    }
    
    //Formats all the content needed for the email and the display on the page
    class func formatContent(showTo:String, email:NSManagedObject) -> String {
        
        var commentsFormatted: String = email.valueForKey("comments") as! String
        
        var dbscEmail:String = {
            if checkedServiceAbbr == "CF" {
                return CFEmail
            } else {
                return SSPEmail
            }
        }()
        
        var emailBottomDescription: String = "\n__\n\nThis is your digital record of services providedfor the Deaf-Blind Service Center. If you have any questions, don't believe you should have recieved this email, or you find something inaccurate in the content, please email us immediately at \(dbscEmail)."
        
        if showTo == "provider" {
            
            return "Thank you from DBSC! The following is a record of your service:\n\n\(commentsFormatted)Date: \(dateString) \nStart Time: \(startTime) \nEnd Time: \(endTime) \nTotal Time: \(timeTotal) \n\nService: \(checkedService) \n\(checkedServiceAbbr): \(user) \nClient: \(client) \(emailBottomDescription)"
            
        } else if showTo == "client" {
            
            return "Hello from DBSC! The following is a record of your service:\n\n\(commentsFormatted)Date: \(dateString) \nStart Time: \(startTime) \nEnd Time: \(endTime) \nTotal Time: \(timeTotal) \n\nService: \(checkedService) \n\(checkedServiceAbbr): \(user) \nClient: \(client) \(emailBottomDescription)"
            
        } else if showTo == "dbsc" {
            
            return "\(commentsFormatted)Date: \(dateString) \nStart Time: \(startTime) \nEnd Time: \(endTime) \nTotal Time: \(timeTotal) \n\nService: \(checkedService) \n\(checkedServiceAbbr): \(user) \n\(checkedServiceAbbr) Email: \(userEmail) \nClient: \(client) \nClient Email: \(clientEmail) \nClient Code: \(clientCode)"
            
        } else if showTo == "inapp" {
            
            return "\(commentsFormatted)Date: \(dateString) \nStart Time: \(startTime) \nEnd Time: \(endTime) \nTotal Time: \(timeTotal) \n\nService: \(checkedService) \n\(checkedServiceAbbr): \(user) \nClient: \(client)"
        }
        
        return ""
        
    }
    
    //Stores the email in core data for "Email" entity
    class func storeEmail() {
        
        let appDel:AppDelegate = UIApplication.sharedApplication().delegate as! AppDelegate
        
        let context:NSManagedObjectContext = appDel.managedObjectContext!
        
        var commentsFormatted: String = EmailFunctions.formatComments()
        
        storedEmail = NSEntityDescription.insertNewObjectForEntityForName("Email", inManagedObjectContext: context) as? NSManagedObject
        storedEmail?.setValue(client, forKey: "client")
        storedEmail?.setValue(clientEmail, forKey: "clientEmail")
        storedEmail?.setValue(clientCode, forKey: "clientCode")
        storedEmail?.setValue(user, forKey: "user")
        storedEmail?.setValue(userEmail, forKey: "userEmail")
        storedEmail?.setValue(commentsFormatted, forKey: "comments")
        storedEmail?.setValue(dateString, forKey: "dateString")
        storedEmail?.setValue(startTime, forKey: "startTime")
        storedEmail?.setValue(endTime, forKey: "endTime")
        storedEmail?.setValue(timeTotal, forKey: "timeTotal")
        storedEmail?.setValue(checkedService, forKey: "checkedService")
        storedEmail?.setValue(checkedServiceAbbr, forKey: "checkedServiceAbbr")
        storedEmail?.setValue(false, forKey: "sent")
        storedEmail?.setValue(false, forKey: "finished")
        storedEmail?.setValue(NSDate(), forKey: "date")
        
        //Saves context, throws error if there's a problem
        var error: NSError?
        if !context.save(&error) {
            println("Could not save \(error), \(error?.userInfo)")
        }
    }
    
    class func updateEmail (sent: Bool) {
        
        let appDel:AppDelegate = UIApplication.sharedApplication().delegate as! AppDelegate
        
        let context:NSManagedObjectContext = appDel.managedObjectContext!
        
        var commentsFormatted: String = EmailFunctions.formatComments()
        
        storedEmail?.setValue(commentsFormatted, forKey: "comments")
        storedEmail?.setValue(sent, forKey: "sent")
        storedEmail?.setValue(endTime, forKey: "endTime")
        storedEmail?.setValue(timeTotal, forKey: "timeTotal")
        
        //Saves context, throws error if there's a problem
        var error: NSError?
        if !context.save(&error) {
            println("Could not update: \(error), \(error?.userInfo)")
        }
    }
}

