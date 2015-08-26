//
//  ViewController.swift
//  SSP Service Tracker
//
//  Created by Connor Hargus on 7/6/15.
//  Copyright (c) 2015 AppMaker. All rights reserved.
//

import UIKit
import CoreData

var user:String = ""
var userEmail:String = ""
var client:String = ""
var clientCode:String = "No ID Card Scanned"
var clientEmail:String = ""
var checkedService:String = ""
var checkedServiceAbbr:String = ""
var timeTotal:String = "Unknown"
var startTime:String = ""
var startDate:NSDate? = nil
var endTime:String = "Unknown"
var dateString:String = ""
var seconds:Int = 0

var notified:Bool = false
var finishing:Bool = false
var selectedCell:UITableViewCell?
var QRInfo:[String] = [""]
var scanNumber:String = "First"
var comments:[String] = []
var commentTimes:[String] = []
var storedEmail: NSManagedObject?

//Preset comments and comment times for testing
//var comments:[String] = ["Nevermind they found it.", "My client can't find their ID card, I think I'll go ahead and press lost ID so that we can move forward.", "They were late by 10 minutes."]
//var commentTimes:[String] = ["3:46 PM, Aug 6, 2015", "3:10 PM, Aug 6, 2015", "3:00 PM, Aug 6, 2015"]
//var comments:[String] = ["A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R"]
//var commentTimes:[String] = ["3:47 PM, Aug 6, 2015","3:47 PM, Aug 6, 2015","3:46 PM, Aug 6, 2015","3:46 PM, Aug 6, 2015","3:46 PM, Aug 6, 2015","3:46 PM, Aug 6, 2015","3:46 PM, Aug 6, 2015","3:46 PM, Aug 6, 2015","3:46 PM, Aug 6, 2015","3:46 PM, Aug 6, 2015","3:46 PM, Aug 6, 2015","3:46 PM, Aug 6, 2015","3:46 PM, Aug 6, 2015","3:46 PM, Aug 6, 2015","3:46 PM, Aug 6, 2015","3:46 PM, Aug 6, 2015","3:46 PM, Aug 6, 2015","3:45 PM, Aug 6, 2015"]

var CFEmail: String = ""
var SSPEmail: String = ""

class ViewController: UIViewController, UINavigationControllerDelegate, UIImagePickerControllerDelegate, UITableViewDelegate, UITableViewDataSource {
    
    @IBOutlet weak var errorLabel: UILabel!
    
    //Service choices for the provider
    var services: [String] = ["Communication Facilitator (CF)", "Support Service Provider (SSP)"]
    
    @IBOutlet weak var servicesTableView: UITableView!
    
    @IBOutlet weak var beginServiceButton: UIButton!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        startDate = nil
        seconds = 0
        comments = []
        commentTimes = []
        selectedCell = nil
        storedEmail = nil
        
        //Gets DBSC mail information from text file
        if let path = NSBundle.mainBundle().pathForResource("DBSCINFORMATIONHERE", ofType: "txt") {

            if let text = String(contentsOfFile: path, encoding: NSUTF8StringEncoding, error: nil) {
                    
                var contentArray = text.componentsSeparatedByString("CF Coordinator: ")
                var emailInfoArray = contentArray[1].componentsSeparatedByString("\nSSP Coordinator: ")
                CFEmail = emailInfoArray[0]
                SSPEmail = emailInfoArray[1]
            }
        }
        
        //Resets checked service when page is loaded
        checkedService = ""
        
    }
    
    override func viewWillAppear(animated: Bool) {
        
        //Checks for unfinished services, asks whether to keep them
        var appDel:AppDelegate = UIApplication.sharedApplication().delegate as! AppDelegate
        
        var context:NSManagedObjectContext = appDel.managedObjectContext!
        
        var request = NSFetchRequest(entityName: "Email")
        
        request.returnsObjectsAsFaults = false
        
        request.predicate = NSPredicate(format: "finished = %@", false)
        
        var emails = context.executeFetchRequest(request, error: nil)
        
        if(emails?.count > 0) {
            
            for email: AnyObject in emails! {
                    
                var alert = UIAlertController(title: "Process unfinished service?", message: "It seems the app quit unexpectedly before a previous service could be processed. Would you like to process this service and add it to the record?", preferredStyle: UIAlertControllerStyle.Alert)
                
                alert.addAction(UIAlertAction(title: "No", style: .Default, handler: {
                    (alert: UIAlertAction!) -> Void in
                    
                    context.deleteObject(email as! NSManagedObject)
                    context.save(nil)
                    
                    self.sendUnsentEmails()
                    
                }))
                
                alert.addAction(UIAlertAction(title: "Yes", style: .Default, handler: {
                    (alert: UIAlertAction!) -> Void in
                    
                    EmailFunctions.updateGlobalVariables(email as? NSManagedObject)
                    finishing = true
                    
                    self.performSegueWithIdentifier("finishUnfinished", sender: self)
                    
                    //self.sendUnsentEmails()
                    
                }))
                self.presentViewController(alert, animated:true, completion:nil)
            
            }
        } else {
            
            self.sendUnsentEmails()
        }
    }
    
    override func viewDidLayoutSubviews() {
        
        //Gets rid of the line between comment cells
        //servicesTableView.separatorStyle = UITableViewCellSeparatorStyle.None
        servicesTableView.rowHeight = 35
        
        //Registers the default "cell"
        servicesTableView.registerClass(UITableViewCell.self, forCellReuseIdentifier: "cell")
        
        //Lays out begin service button
        beginServiceButton.layer.borderWidth = 0.75
        beginServiceButton.layer.borderColor = UIColor(red: 0, green: 0.478431 , blue: 1.0, alpha: 1.0).CGColor
        beginServiceButton.layer.cornerRadius = 3.0
        
        servicesTableView.layer.borderWidth = 0.75
        servicesTableView.layer.borderColor = UIColor(red: 0.75, green: 0.75 , blue: 0.75, alpha: 1.0).CGColor
        servicesTableView.layer.cornerRadius = 3.0
        
        //Resets error label
        errorLabel.alpha = 0
        
    }
    
    //Checks for unsent emails, sends them
    func sendUnsentEmails () {
        
        var appDel:AppDelegate = UIApplication.sharedApplication().delegate as! AppDelegate
        
        var context:NSManagedObjectContext = appDel.managedObjectContext!
        
        var request = NSFetchRequest(entityName: "Email")
        
        request.returnsObjectsAsFaults = false
        
        request.predicate = NSPredicate(format: "sent = %@", false)
        
        var emails = context.executeFetchRequest(request, error: nil)
        
        if(emails?.count > 0) {
        
            if Reachability.isConnectedToNetwork() == true {
                
                var allSent:Bool = false
                
                for email: AnyObject in emails! {
                    
                    EmailFunctions.updateGlobalVariables(email as? NSManagedObject)
                    
                    var providerContent = EmailFunctions.formatContent("provider", email: storedEmail!)
                    var clientContent = EmailFunctions.formatContent("client", email: storedEmail!)
                    var dbscContent = EmailFunctions.formatContent("dbsc", email: storedEmail!)
                    
                    var dbscEmail:String = {
                        if checkedServiceAbbr == "CF" {
                            return CFEmail
                        } else {
                            return SSPEmail
                        }
                    }()
                    
                    if EmailFunctions.sendEmail(userEmail, content: providerContent) && EmailFunctions.sendEmail(clientEmail, content: clientContent) && EmailFunctions.sendEmail(dbscEmail, content: dbscContent) {
                        
                        email.setValue(true, forKey: "sent")
                        context.save(nil)
                    
                    }
                }
                
                var alert = UIAlertController(title: "Past services processed", message: "Past services performed without internet access have now been processed with DBSC.", preferredStyle: UIAlertControllerStyle.Alert)
                
                alert.addAction(UIAlertAction(title: "Okay", style: .Default, handler: nil))
                
                self.presentViewController(alert, animated:true, completion:nil)
                
            } else if !notified {
                
                notified = true
                
                var alert = UIAlertController(title: "Reminder: Past services waiting to process", message: "Please open the app again when internet access is regained to ensure speedy processing of your services.", preferredStyle: UIAlertControllerStyle.Alert)
                
                alert.addAction(UIAlertAction(title: "Okay", style: .Default, handler: nil))
                
                self.presentViewController(alert, animated:true, completion:nil)
            }
        }
        
        EmailFunctions.updateGlobalVariables(nil)
        if let cell = selectedCell {
            checkedService = cell.textLabel!.text!
        }
    }
    
    
    
    //Check whether settings page is filled out
    func checkSettings() -> Bool {
        
        if checkedService == "" {
            errorLabel.text = "Please select a service"
            errorLabel.alpha = 1
            
        } else if user == "" {
            
            errorLabel.text = "Please enter your name in \"Settings\""
            errorLabel.alpha = 1
            
        } else if userEmail == "" {
            
            errorLabel.text = "Please enter your email in \"Settings\""
            errorLabel.alpha = 1
            
        } else {
            
            return true
        }
        
        return false
    }
    
    //Start button pressed
    @IBAction func verifyPressed(sender: AnyObject) {
        
        if checkSettings() {
            
            performSegueWithIdentifier("takeQRShot", sender: self)
        }
    }
    
    //Allows user to continue by pressing "Missing ID?" and entering their client's name
    @IBAction func lostIDPressed(sender: AnyObject) {
        
        if checkSettings() {
            
            var inputTextField: UITextField?
        
            var alert = UIAlertController(title: "Missing Client ID Card?", message: "Please enter your client's name and press \"Continue\" if so.", preferredStyle: UIAlertControllerStyle.Alert)
            
            alert.addTextFieldWithConfigurationHandler({ (textField) -> Void in
                inputTextField = textField
                inputTextField!.placeholder = "Enter client name here"
                inputTextField!.addTarget(self, action: "textChanged:", forControlEvents: .EditingChanged)
            })
            
            alert.addAction(UIAlertAction(title: "Cancel", style: .Default, handler: nil))
            
            alert.addAction(UIAlertAction(title: "Continue", style: .Default, handler: { (alert) -> Void in
                
                client = inputTextField!.text
                self.performSegueWithIdentifier("lostIDSkip", sender: self)
            }))
            
            (alert.actions[1] as! UIAlertAction).enabled = false
            
            self.presentViewController(alert, animated:true, completion:nil)
        }
    }
    
    func textChanged(sender:AnyObject) {
        let tf = sender as! UITextField
        var resp : UIResponder = tf
        while !(resp is UIAlertController) { resp = resp.nextResponder()! }
        let alert = resp as! UIAlertController
        (alert.actions[1] as! UIAlertAction).enabled = (tf.text != "")
    }
    
        //Closes keyboard when touched outside
    override func touchesBegan(touches: Set<NSObject>, withEvent event: UIEvent) {
        self.view.endEditing(true)
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        // #warning Potentially incomplete method implementation.
        // Return the number of sections.
        return 1
    }
    
    func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        // #warning Incomplete method implementation.
        // Return the number of rows in the section.
        
        return self.services.count
    }

    
    func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        
        //Sets up the service choices table
        let tableCell = tableView.dequeueReusableCellWithIdentifier("cell", forIndexPath: indexPath) as! UITableViewCell
        
        tableCell.textLabel!.text = self.services[indexPath.row]
        
        /*if indexPath.row == 1 {
            tableCell.separatorInset = UIEdgeInsetsMake(0, tableCell.bounds.size.width, 0, 0)
        }
        
        if(indexPath.row == 1) {
            tableCell.separatorInset = UIEdgeInsetsMake(0, 0, 0, CGRectGetWidth(self.servicesTableView.bounds));
        }*/
        
        
        return tableCell
    }
    
    //Sets inset for the separator between cells
    func tableView(tableView: UITableView, willDisplayCell cell: UITableViewCell, forRowAtIndexPath indexPath: NSIndexPath) {
        
        if cell.respondsToSelector("setSeparatorInset:") {
            cell.separatorInset = UIEdgeInsetsZero
        }
        if cell.respondsToSelector("setLayoutMargins:") {
            cell.layoutMargins = UIEdgeInsetsZero
        }
        if cell.respondsToSelector("setPreservesSuperviewLayoutMargins:") {
            cell.preservesSuperviewLayoutMargins = false
        }
    }
    
    //Gives the cell a checkmark and sets it as the checkedService when tapped
    func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        
        self.view.endEditing(true)
        
        errorLabel.alpha = 0
        
        var cell: UITableViewCell = tableView.cellForRowAtIndexPath(indexPath)!
        
        checkedService = self.services[indexPath.row]
        
        selectedCell = cell
        
        cell.accessoryType = UITableViewCellAccessoryType.Checkmark
        
    }
    
    //Unchecks the checked cell when another cell is tapped
    func tableView(tableView: UITableView, didDeselectRowAtIndexPath indexPath: NSIndexPath) {
        
        var otherCell: UITableViewCell = tableView.cellForRowAtIndexPath(indexPath)!
        
        otherCell.accessoryType = UITableViewCellAccessoryType.None
    }


}

