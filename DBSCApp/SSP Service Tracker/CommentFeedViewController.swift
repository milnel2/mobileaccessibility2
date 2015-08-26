//
//  OtherViewController.swift
//  SSP Service Tracker
//
//  Created by Connor Hargus on 7/8/15.
//  Copyright (c) 2015 AppMaker. All rights reserved.
//

import UIKit
import Foundation

//This class is responsible for the comment section which appears in multiple places in a container view
@IBDesignable

class CommentFeedViewController: UIViewController, UITextViewDelegate, UITableViewDelegate, UITableViewDataSource {

    @IBInspectable var borderColor:UIColor = UIColor(red: 0.75, green: 0.75, blue: 0.75, alpha: 1.0)
    @IBInspectable var buttonColor:UIColor = UIColor(red: 0, green: 0.478431 , blue: 1.0, alpha: 1.0)
    
    @IBOutlet weak var commentsTable: UITableView!
    @IBOutlet weak var submitButton: UIButton!
    @IBOutlet weak var commentBoxTopConstraint: NSLayoutConstraint!
    @IBOutlet weak var descriptionLabel: UILabel!
    @IBOutlet weak var commentBox: UITextView!
    
    var commentBoxConstraintConstant:CGFloat = 0.0
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        //Sets the height of the row to fit text boxes
        commentsTable.estimatedRowHeight = self.commentsTable.rowHeight
        commentsTable.rowHeight = UITableViewAutomaticDimension
        
        //Observes for keyboard changes to move the text view
        NSNotificationCenter.defaultCenter().addObserver(self, selector: Selector("keyboardWillChange:"), name:UIKeyboardWillChangeFrameNotification, object: nil);
        
        commentBoxConstraintConstant = commentBoxTopConstraint.constant
    }
    
    deinit {
        NSNotificationCenter.defaultCenter().removeObserver(self);
    }
    
    override func viewWillAppear(animated: Bool) {
        commentsTable.reloadData()
        
        if let parentVC = self.parentViewController as? SummaryViewController {
            if finishing == false {
                descriptionLabel.text = "Please leave any final comments and/or delete unwanted ones before processing:"
            } else {
                descriptionLabel.text = "In addition to any comments on the service, please tell us the service's end time:"
            }
        } else {
            descriptionLabel.text = "Leave comments in our record for this service if anything unexpected occurs:"
        }
        
        
    }
    
    override func viewDidLayoutSubviews() {
        
        //Border for top of comment table
        var border = CALayer()
        //border.frame = CGRectMake(0, 0, CGRectGetWidth(commentsTable.layer.frame), 1)
        border.frame = CGRectMake(0, 0, super.view.frame.width, 1)
        border.backgroundColor = borderColor.CGColor
        commentsTable.layer.addSublayer(border)
        
        //Border for the comment box
        commentBox.layer.borderWidth = 1
        commentBox.layer.borderColor = borderColor.CGColor
        commentBox.delegate = self
        self.view.bringSubviewToFront(commentBox) //Uncovers commentBox from other views
        
        //Gives the container for the comment section a border
        self.view.layer.borderWidth = 0.75
        self.view.layer.borderColor = borderColor.CGColor
        self.view.layer.cornerRadius = 3.0
        
        //Gets rid of the line between comment cells
        commentsTable.separatorStyle = UITableViewCellSeparatorStyle.None
        commentsTable.backgroundView = nil
        
        //Adds line to top of table only (see CustomBorderTop.swift)
        //commentsTable.layoutMargins.top
        //commentsTable.layer.addBorder(UIRectEdge.Top, color: borderColor, thickness: 1)
        //commentsTable.layer.borderWidth = 0.75
        //commentsTable.layer.borderColor = buttonColor.CGColor
        //commentsTable.layer.cornerRadius = 3.0
        
        //Sets up button styling
        submitButton.layer.borderWidth = 0.75
        submitButton.layer.borderColor = buttonColor.CGColor
        submitButton.layer.cornerRadius = 3.0

        
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    //Gets rid of keyboard when somewhere else in container is tapped (note a separate version for the area outside the container in ViewController
    override func touchesBegan(touches: Set<NSObject>, withEvent event: UIEvent) {
        
        self.view.endEditing(true)
        
    }
    
    //Action for when a comment is submitted
    @IBAction func submitPressed(sender: AnyObject) {
        
        if commentBox.text != "" {
            
            //Inserts comments at the beginning of the array to preserve chronological order with newest at the top
            comments.insert(commentBox.text, atIndex: 0)
            
            //Gets the time in a nice format
            let date = NSDate()
            let formatter = NSDateFormatter()
            //formatter.dateStyle = .MediumStyle
            //formatter.timeStyle = .ShortStyle
            formatter.dateFormat = "h:mm a, MMM d, YYYY"
            commentTimes.insert(formatter.stringFromDate(date), atIndex: 0)
            
            commentBox.text = ""
            
            commentsTable.reloadData()
            
            self.reloadSummary()
            
            //Updates the service log
            if let parentVC = self.parentViewController as? SummaryViewController {
                
                EmailFunctions.updateEmail(false)
                
            } else if let parentVC2 = self.parentViewController as? ServiceRunningViewController {
                
                EmailFunctions.updateEmail(false)
                
            }
            //Scrolls back to top of table
            let indexPath = NSIndexPath(forRow: 0, inSection: 0)
            commentsTable.scrollToRowAtIndexPath(indexPath, atScrollPosition: .Top, animated: true)
        }
        
    }
    
    //Updates the final summary preview on the summary page
    func reloadSummary() {
    
        if let parentVC = self.parentViewController as? SummaryViewController {
            NSNotificationCenter.defaultCenter().postNotificationName("load", object: nil)
            
        }
    }
    
    
    func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        // #warning Potentially incomplete method implementation.
        // Return the number of sections.
        return 1
    }
    
    func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        
        //Uses prototype cell from Interface Builder called "CommentTableCell"
        let tableCell = tableView.dequeueReusableCellWithIdentifier("CommentTableCell", forIndexPath: indexPath) as! CommentTableCell
        tableCell.userInteractionEnabled = true
        tableCell.selectionStyle = .None
        //Sets the text for the cells in the comment table
        tableCell.commentText.text = comments[indexPath.row]
        tableCell.timeLabel.text = commentTimes[indexPath.row]
        
        return tableCell
        
    }
    
    
    //As many rows in the table as there are comments
    func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        
        return comments.count
        
    }
    
    //Allows the user to delete comments
    func tableView(tableView: UITableView, commitEditingStyle editingStyle: UITableViewCellEditingStyle, forRowAtIndexPath indexPath: NSIndexPath) {
        
        if (editingStyle == UITableViewCellEditingStyle.Delete) {
            
            comments.removeAtIndex(indexPath.row)
            commentsTable.deleteRowsAtIndexPaths([indexPath],  withRowAnimation: UITableViewRowAnimation.Automatic)
            
            self.reloadSummary()
        }
        
    }
    
    //Closes the keyboard when the return "Done" key is pressed
    func textView(textView: UITextView, shouldChangeTextInRange range: NSRange, replacementText text: String) -> Bool {
        if(text == "\n") {
            textView.resignFirstResponder()
            return false
        }
        return true
    }
    
    /*func keyboardWasShown(notification: NSNotification) {
        var info = notification.userInfo!
        var keyboardFrame: CGRect = (info[UIKeyboardFrameEndUserInfoKey] as! NSValue).CGRectValue()
        
        UIView.animateWithDuration(1, animations: { () -> Void in
            self.commentBoxTopConstraint.constant = 10
        })
    }*/
    
    func keyboardWillChange(notification: NSNotification) {
        /*
        var info = notification.userInfo!
        var keyboardFrame: CGRect = (info[UIKeyboardFrameEndUserInfoKey] as! NSValue).CGRectValue()
        
        UIView.animateWithDuration(2, animations: { () -> Void in
        self.bottomConstraint.constant = 250
        })
        */
        
        //Moves the comment textview if the keyboard gets in the way
        if let parentVC = (self.parentViewController as? SummaryViewController) {
            return
        
        }
        
        if let userInfo = notification.userInfo {
            let endFrame = (userInfo[UIKeyboardFrameEndUserInfoKey] as? NSValue)?.CGRectValue()
            let duration:NSTimeInterval = (userInfo[UIKeyboardAnimationDurationUserInfoKey] as? NSNumber)?.doubleValue ?? 0
            let animationCurveRawNSN = userInfo[UIKeyboardAnimationCurveUserInfoKey] as? NSNumber
            let animationCurveRaw = animationCurveRawNSN?.unsignedLongValue ?? UIViewAnimationOptions.CurveEaseInOut.rawValue
            let animationCurve:UIViewAnimationOptions = UIViewAnimationOptions(rawValue: animationCurveRaw)
            if endFrame?.origin.y >= UIScreen.mainScreen().bounds.size.height {
                self.commentBoxTopConstraint?.constant = commentBoxConstraintConstant
            } else {
                self.commentBoxTopConstraint?.constant = min(self.view.frame.height - endFrame!.size.height - 40, commentBoxConstraintConstant)
            }
            
            UIView.animateWithDuration(duration,
                delay: NSTimeInterval(0),
                options: animationCurve,
                animations: { self.view.layoutIfNeeded() },
                completion: nil)
        }
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