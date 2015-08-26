//
//  SettingsViewController.swift
//  SSP Service Tracker
//
//  Created by Connor Hargus on 7/8/15.
//  Copyright (c) 2015 AppMaker. All rights reserved.
//

import UIKit
import Foundation
import CoreData

class SettingsViewController: UIViewController, UITextFieldDelegate {
    
    @IBOutlet weak var saveButton: UIButton!
    
    @IBOutlet weak var nameLabel: UITextField!
    @IBOutlet weak var emailLabel: UITextField!
    @IBOutlet weak var savedLabel: UILabel!
    @IBOutlet weak var deleteButton: UIButton!
    @IBOutlet weak var privacyButton: UIButton!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        if user != "" {
            nameLabel.text = user
        }
        if userEmail != "" {
            emailLabel.text = userEmail
        }
        if user != "" && userEmail != "" {
            savedLabel.alpha = 1
        }
        
        //navigationController?.prompt = "Please edit your information here"
        
        nameLabel.delegate = self
        emailLabel.delegate = self
        
        var button = UIBarButtonItem(title: "Settings", style: UIBarButtonItemStyle.Plain, target: self, action: "goBack")
        self.navigationItem.backBarButtonItem = button
    }
    
    func goBack() {
        
        self.navigationController?.popViewControllerAnimated(true)
    }
    
    override func viewDidLayoutSubviews() {
        
        //Lays out save information button
        saveButton.layer.borderWidth = 0.75
        saveButton.layer.borderColor = UIColor(red: 0, green: 0.478431 , blue: 1.0, alpha: 1.0).CGColor
        saveButton.layer.cornerRadius = 3.0
        
        //Lays out delete records button
        deleteButton.layer.borderWidth = 0.75
        deleteButton.layer.borderColor = deleteButton.titleLabel?.textColor.CGColor
        deleteButton.layer.cornerRadius = 3.0
        
        //Lays out privacy record button
        privacyButton.layer.borderWidth = 0.75
        privacyButton.layer.borderColor = privacyButton.titleLabel?.textColor.CGColor
        privacyButton.layer.cornerRadius = 3.0
    }
    
    //Handles the deletion of all data
    @IBAction func deletePressed(sender: AnyObject) {
        
        var alert = UIAlertController(title: "Delete all service records?", message: "Records will be permanently deleted. If you have performed services for which you have not received a confirmation email, press \"No\".", preferredStyle: UIAlertControllerStyle.Alert)
        
        alert.addAction(UIAlertAction(title: "No", style: .Default, handler: nil))
        
        alert.addAction(UIAlertAction(title: "Yes", style: .Default, handler: { (alert) -> Void in
            
            var appDel:AppDelegate = UIApplication.sharedApplication().delegate as! AppDelegate
            
            var context:NSManagedObjectContext = appDel.managedObjectContext!
            
            var request = NSFetchRequest(entityName: "Email")
            
            request.returnsObjectsAsFaults = false
            
            var emails = context.executeFetchRequest(request, error: nil)
            
            if(emails?.count > 0) {
                
                for email:AnyObject in emails! {
                    
                    context.deleteObject(email as! NSManagedObject)
                    context.save(nil)
                }
            }
            
            self.nameLabel.text = ""
            self.emailLabel.text = ""
            
            self.savePressed(self)

        }))
        
        self.presentViewController(alert, animated:true, completion:nil)
    }
    
    @IBAction func privacyPressed(sender: AnyObject) {
        
        performSegueWithIdentifier("seePrivacyPolicy", sender: self)
    }
    
    //Saves settings
    @IBAction func savePressed(sender: AnyObject) {
        
        if user != nameLabel.text {
            
            user = nameLabel.text
            
            NSUserDefaults.standardUserDefaults().setObject(nameLabel.text, forKey: "userName")
            
            
        }
        
        if userEmail != emailLabel.text {
            
            userEmail = emailLabel.text
            
            NSUserDefaults.standardUserDefaults().setObject(emailLabel.text, forKey: "userEmail")
        }
        
        NSUserDefaults.standardUserDefaults().synchronize()
        
        savedLabel.alpha = 1
        
    }
    
    //Removes the "Saved" label when information is edited
    func textFieldDidBeginEditing(textField: UITextField) {
        
        savedLabel.alpha = 0
    }
    
    //Closes the keyboard when the return "Done" key is pressed
    func textFieldShouldReturn(textField: UITextField) -> Bool {
        return textField.resignFirstResponder()
    }
    
    //Closes keyboard when screen is touched outside of keyboard
    override func touchesBegan(touches: Set<NSObject>, withEvent event: UIEvent) {
        self.view.endEditing(true)
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    

    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        // Get the new view controller using segue.destinationViewController.
        // Pass the selected object to the new view controller.
    }
    */

}
