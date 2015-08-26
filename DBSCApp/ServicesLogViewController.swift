//
//  ServicesLogViewController.swift
//  SSP Service Tracker
//
//  Created by Connor Hargus on 7/28/15.
//  Copyright (c) 2015 AppMaker. All rights reserved.
//

import UIKit
import CoreData

class ServicesLogViewController: UIViewController, UITableViewDelegate, UITableViewDataSource, UISearchResultsUpdating, UISearchBarDelegate {

    @IBOutlet weak var pastServicesTable: UITableView!
    
    var services: [(String, String, Bool, NSManagedObject)] = []
    var filteredServices: [(String, String, Bool, NSManagedObject)] = []
    var resultSearchController = UISearchController()
    
    override func viewDidLoad() {
        
        pastServicesTable.tableFooterView = UIView()
        
        //Gets rid of white space at bottom of table for some reason
        pastServicesTable.rowHeight = 400
        
    }
    
    override func viewWillAppear(animated: Bool) {
        
        services = []
        
        var appDel:AppDelegate = UIApplication.sharedApplication().delegate as! AppDelegate
        
        var context:NSManagedObjectContext = appDel.managedObjectContext!
        
        var request = NSFetchRequest(entityName: "Email")
        
        let sortDescriptor = NSSortDescriptor(key: "date", ascending: false)
        request.sortDescriptors = [sortDescriptor]
        
        request.returnsObjectsAsFaults = false
        
        var emails = context.executeFetchRequest(request, error: nil)
        
        if(emails?.count > 0) {
            
            for email:AnyObject in emails! {
                
                let email = email as? NSManagedObject
                
                EmailFunctions.updateGlobalVariables(email)
                
                services.append(EmailFunctions.formatContent("inapp", email: email!), EmailFunctions.formatSubject(email!), email!.valueForKey("sent") as! Bool, email!)
            }
        }
        
        self.resultSearchController = ({
            let controller = UISearchController(searchResultsController: nil)
            controller.searchResultsUpdater = self
            controller.dimsBackgroundDuringPresentation = false
            controller.searchBar.sizeToFit()
            controller.searchBar.placeholder = "Search by keyword, name, etc."
            controller.searchBar.barTintColor = UIColor(red: 224/256, green: 244/256, blue: 255/256, alpha: 1)
            controller.searchBar.delegate = self
            
            self.pastServicesTable.tableHeaderView = controller.searchBar
            
            return controller
        })()
        
        
        pastServicesTable.estimatedRowHeight = self.pastServicesTable.rowHeight
        pastServicesTable.rowHeight = UITableViewAutomaticDimension
        
        //Gets rid of the line between comment cells
        pastServicesTable.separatorStyle = UITableViewCellSeparatorStyle.None
        
        pastServicesTable.reloadData()
        
        
        
    }
    
    override func viewDidAppear(animated: Bool) {
        
        
    }
    
    // MARK - Helpers methods
    func filterContentForSearchText(searchText: String) {
        self.filteredServices = self.services.filter({(service:(String, String, Bool, NSManagedObject)) -> Bool in
            
            let subjectMatch = service.1.uppercaseString.rangeOfString(searchText.uppercaseString)
            let contentMatch = service.0.uppercaseString.rangeOfString(searchText.uppercaseString)
            return (subjectMatch != nil) || (contentMatch != nil)
        })
    }
    
    // MARK: - UISearchControllerDelegate
    func updateSearchResultsForSearchController(searchController: UISearchController) {
        
        filteredServices = services
        pastServicesTable.separatorStyle = UITableViewCellSeparatorStyle.None
        filterContentForSearchText(searchController.searchBar.text)
        pastServicesTable.reloadData()
    }
    
    func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        
        //Uses prototype cell from Interface Builder called "CommentTableCell"
        let tableCell = tableView.dequeueReusableCellWithIdentifier("serviceCell", forIndexPath: indexPath) as! PastServiceCell
        tableCell.userInteractionEnabled = true
        tableCell.selectionStyle = .None
        
        //Sets the text for the cells in the comment table
        if services.count == 0 {
            
            tableCell.serviceTextView.alpha = 0
            tableCell.serviceTitleView.text = "No past services recorded."
            tableCell.userInteractionEnabled = false
        }
        else {
            if services.count >= 2 {
                pastServicesTable.separatorStyle = UITableViewCellSeparatorStyle.SingleLine
            }
        
            let service:(String, String, Bool, NSManagedObject)
            
            //Displays different table if searching is active
            if (self.resultSearchController.active) {
                service = self.filteredServices[indexPath.row]
            } else {
                service = self.services[indexPath.row]
            }
            tableCell.serviceTextView.text = service.0
            tableCell.serviceTitleView.text = service.1
            if services[indexPath.row].2 == true {
                
                tableCell.sentTextView.alpha = 1
            }
        }
        return tableCell
    }
    
    //Sets inset for the separator between cells
    func tableView(tableView: UITableView, willDisplayCell cell: UITableViewCell, forRowAtIndexPath indexPath: NSIndexPath) {
            
        if cell.respondsToSelector("setSeparatorInset:") {
            cell.separatorInset = UIEdgeInsetsMake(0, 30, 0, 30)
        }
        if cell.respondsToSelector("setLayoutMargins:") {
            cell.layoutMargins = UIEdgeInsetsMake(0, 30, 0, 30)
        }
        if cell.respondsToSelector("setPreservesSuperviewLayoutMargins:") {
            cell.preservesSuperviewLayoutMargins = false
        }
    }
    
    //As many rows in the table as there are services, depending on searching or not.
    func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        
        if (self.resultSearchController.active) {
            return filteredServices.count
        } else {
            return max(1, services.count)
        }
    }
    
    
    //Closes the keyboard when the return "Search" key is pressed
    func textView(textView: UITextView, shouldChangeTextInRange range: NSRange, replacementText text: String) -> Bool {
        if(text == "\n") {
            textView.resignFirstResponder()
            return false
        }
        return true
    }
    
    //Closes keyboard on outside touch
    override func touchesBegan(touches: Set<NSObject>, withEvent event: UIEvent) {
        
        self.view.endEditing(true)
        
    }
    
    //Allows the user to resend records
    func tableView(tableView: UITableView, commitEditingStyle editingStyle: UITableViewCellEditingStyle, forRowAtIndexPath indexPath: NSIndexPath) {
        
    }
    
    func tableView(tableView: UITableView, editActionsForRowAtIndexPath indexPath: NSIndexPath) -> [AnyObject]?  {
        
        var resendAction = UITableViewRowAction(style: UITableViewRowActionStyle.Default, title: "Resend?" , handler: { (action:UITableViewRowAction!, indexPath:NSIndexPath!) -> Void in
            
            self.resendEmail(indexPath.row)
        })
        
        resendAction.backgroundColor = UIColor(red: 0, green: 0, blue: 1, alpha: 0.5)
        
        return [resendAction]
    }
    
    //Resends emails after the user presses resend on a table cell
    func resendEmail(indexPath:Int) {
        
        var alert = UIAlertController(title: "Reprocess this service?", message: "Please only resend if you did not receive a confirmation email.", preferredStyle: UIAlertControllerStyle.Alert)
        
        alert.addAction(UIAlertAction(title: "No", style: .Default, handler: nil))
        
        alert.addAction(UIAlertAction(title: "Yes", style: .Default, handler: {
            (alert: UIAlertAction!) -> Void in
            
            //Checks whether connected to the internet. If true, send emails. If not, tells user
            if Reachability.isConnectedToNetwork() == true {
                
                let email:NSManagedObject
                
                if (self.resultSearchController.active) {
                    
                    email = self.filteredServices[indexPath].3
                    
                } else {
                    
                    email = self.services[indexPath].3
                }
                
                EmailFunctions.updateGlobalVariables(email)
                
                var appDel:AppDelegate = UIApplication.sharedApplication().delegate as! AppDelegate
                
                var context:NSManagedObjectContext = appDel.managedObjectContext!
                
                var alert = UIAlertController()
                
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
                
                if EmailFunctions.sendEmail(userEmail, content: providerContent, resent: true) && EmailFunctions.sendEmail(clientEmail, content: clientContent, resent: true) && EmailFunctions.sendEmail(dbscEmail, content: dbscContent, resent: true) {
                    
                    storedEmail!.setValue(true, forKey: "sent")
                    context.save(nil)
                    
                    //Alert to end and notify of email
                    alert = UIAlertController(title: "Service reprocessed!", message: "Emails successfully resent. Please check your email for a record of this service", preferredStyle: UIAlertControllerStyle.Alert)
                    
                } else {
                    
                    alert = UIAlertController(title: "Unsteady internet access.", message: "Could not be reprocessed, please try again later.", preferredStyle: UIAlertControllerStyle.Alert)
                    
                }
                
                alert.addAction(UIAlertAction(title: "Okay", style: .Default, handler: { (action) -> Void in
                    
                }))
                
                self.presentViewController(alert, animated:true, completion:nil)
                
            } else {
                
                //Alert to describe what to do when internet access is regained
                var alert = UIAlertController(title: "Please try again when you have internet access.", message: "", preferredStyle: UIAlertControllerStyle.Alert)
                
                alert.addAction(UIAlertAction(title: "Okay", style: .Default, handler: { (action) -> Void in
                    
                }))
                
                self.presentViewController(alert, animated:true, completion:nil)
                
            }
            
            EmailFunctions.updateGlobalVariables(nil)
        }))
        
        self.presentViewController(alert, animated:true, completion:nil)
    }
}

