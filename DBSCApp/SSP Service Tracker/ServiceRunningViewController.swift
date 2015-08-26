//
//  ServiceRunningViewController.swift
//  
//
//  Created by Connor Hargus on 7/6/15.
//
//

import UIKit
import CoreData
import Foundation

class ServiceRunningViewController: UIViewController {

    var timer = NSTimer()
    
    @IBOutlet weak var completeButton: UIButton!
    
    @IBOutlet weak var timeLabel: UILabel!
    
    @IBOutlet weak var toLabel: UILabel!
    @IBOutlet weak var byLabel: UILabel!
    @IBOutlet weak var startTimeLabel: UILabel!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        //Attempt at changing accessibility label to inform blind user that an ID card was scanned successfully
        //navigationController?.navigationBar.topItem?.accessibilityLabel = "ID card scanned"
        
    }
    
    override func viewWillAppear(animated: Bool) {
        
        seconds = secondsSinceStart()
        
        if startDate == nil {
            
            //Sets abbreviation of service
            checkedServiceAbbr = {
                if checkedService == "Communication Facilitator (CF)" {
                    return "CF"
                } else {
                    return "SSP"
                }
            }()
            
            //Sets the start time as view appears
            let date = NSDate()
            startDate = date
            let formatter = NSDateFormatter()
            formatter.timeStyle = .ShortStyle
            startTime = formatter.stringFromDate(date)
            
            //Sets the date at beginning of service
            let dateFormatter = NSDateFormatter()
            dateFormatter.dateStyle = .MediumStyle
            dateString = dateFormatter.stringFromDate(date)
            toLabel.text = "Client: " + "\(client)"
            byLabel.text = "\(checkedServiceAbbr): " + "\(user)"
            startTimeLabel.text = "Start Time: \(startTime)"
            
            timer.invalidate()
            
        }
        
        if storedEmail == nil {
            
            EmailFunctions.storeEmail()
        }
        
        
        
        //Sets timer to start counting and calling "changeLabel"
        timer = NSTimer.scheduledTimerWithTimeInterval(1, target: self, selector: "changeLabel", userInfo: nil, repeats: true)
    }
    
    override func viewDidLayoutSubviews() {
        
        //Sets up cancel button for the view
        var button = UIBarButtonItem(title: "Cancel", style: UIBarButtonItemStyle.Plain, target: self, action: "backPressed")
        self.navigationItem.leftBarButtonItem = button
        
        //Lays out complete service button
        completeButton.layer.borderWidth = 0.75
        completeButton.layer.borderColor = UIColor(red: 0, green: 0.478431 , blue: 1.0, alpha: 1.0).CGColor
        completeButton.layer.cornerRadius = 3.0
        
        //Lays out clock border
        timeLabel.layer.borderWidth = 2
        timeLabel.layer.borderColor = timeLabel.textColor.CGColor
        timeLabel.layer.cornerRadius = 3.0
    }
    
    //Updates the time label as the timer is running
    func changeLabel() {
        
        seconds++
        
        var hh = String(format: "%02d", seconds / 3600)
        var mm = String(format: "%02d", (seconds % 3600) / 60)
        var ss = String(format: "%02d", (seconds % 3600) % 60)
        timeLabel.text = hh + ":" + mm + ":" + ss
        
        //If we wanted mm:ss time before an hour had passed.
        /*if seconds >= 3600 {
        
            timeLabel.text = hh + ":" + mm + ":" + ss
            
        } else {
            
            timeLabel.text = mm + ":" + ss
            
        }*/
    }
    
    
    //Reverts back to first QR code scan, asks if they're sure
    func backPressed() {
        
        let cancelMenu = UIAlertController(title: nil, message: "Are you sure you want to cancel this service? Any work thus far will not be saved.", preferredStyle: .ActionSheet)
        
        let cancelAction = UIAlertAction(title: "Cancel Service", style: .Destructive, handler: {
            (alert: UIAlertAction!) -> Void in
            
            startDate = nil
            seconds = 0
            scanNumber = "First"
            comments = []
            selectedCell = nil
            self.timer.invalidate()
            
            //Deletes email if service is cancelled
            let appDel:AppDelegate = UIApplication.sharedApplication().delegate as! AppDelegate
            let context:NSManagedObjectContext = appDel.managedObjectContext!
            if storedEmail != nil {
                context.deleteObject(storedEmail!)
                storedEmail = nil
            }
            
            self.performSegueWithIdentifier("cancelService", sender: self)
        })
        
        let nevermindAction = UIAlertAction(title: "Oops, return to service", style: .Cancel, handler: nil)
        
        cancelMenu.popoverPresentationController?.sourceView = self.view
        cancelMenu.popoverPresentationController?.sourceRect = CGRectMake(timeLabel.frame.origin.x + timeLabel.frame.width / 2, timeLabel.frame.origin.y + timeLabel.frame.height / 2, 1.0, 1.0)
        cancelMenu.addAction(cancelAction)
        cancelMenu.addAction(nevermindAction)
        
        self.presentViewController(cancelMenu, animated: true, completion: nil)
        
    }
    
    func prepareForProceeding() {
        
        scanNumber = "Second"
        
        //Sets the end time when complete is pressed
        let date = NSDate()
        let formatter = NSDateFormatter()
        formatter.timeStyle = .ShortStyle
        endTime = formatter.stringFromDate(date)
        
        timeTotal = self.timeLabel.text!
        
        self.timer.invalidate()
        
    }
    
    @IBAction func completePressed(sender: AnyObject) {
        
        prepareForProceeding()
        
        self.performSegueWithIdentifier("serviceCompleted", sender: self)
        
    }
    
    //Sets up what happens if no ID is found
    @IBAction func lostIDPressed(sender: AnyObject) {
        
        var inputTextField: UITextField?
        
        var alert = UIAlertController(title: "Missing Client ID Card?", message: "", preferredStyle: UIAlertControllerStyle.Alert)
        
        if client == "" {
            alert.addTextFieldWithConfigurationHandler({ (textField) -> Void in
                inputTextField = textField
                inputTextField!.placeholder = "Enter client name here"
                
            })
        }
        
        alert.addAction(UIAlertAction(title: "Back", style: .Default, handler: nil))
        
        alert.addAction(UIAlertAction(title: "Continue", style: .Default, handler: { (alert) -> Void in
            if client == "" {
                client = inputTextField!.text
            }
            self.performSegueWithIdentifier("lostIDSecondSkip", sender: self)
            
            self.prepareForProceeding()
        }))
        
        self.presentViewController(alert, animated:true, completion:nil)

    }
    

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    override func touchesBegan(touches: Set<NSObject>, withEvent event: UIEvent) {
        self.view.endEditing(true)
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
