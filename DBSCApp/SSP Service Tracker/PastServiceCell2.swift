//
//  PastServiceCell2.swift
//  SSP Service Tracker
//
//  Created by Connor Hargus on 7/28/15.
//  Copyright (c) 2015 AppMaker. All rights reserved.
//

import UIKit

class PastServiceCell2: UITableViewCell {
    
    @IBOutlet weak var serviceTextView: UITextView!
    @IBOutlet weak var serviceTitleView: UITextView!
    @IBOutlet weak var sentTextView: UITextView!
    
    override func awakeFromNib() {
        
        var borderColor : UIColor = UIColor(red: 0.75, green: 0.75, blue: 0.75, alpha: 1.0)
        /*
        let fixedWidth = commentText.frame.size.width
        commentText.sizeThatFits(CGSize(width: fixedWidth, height: CGFloat.max))
        let newSize = commentText.sizeThatFits(CGSize(width: fixedWidth, height: CGFloat.max))
        var newFrame = commentText.frame
        newFrame.size = CGSize(width: max(newSize.width, fixedWidth), height: newSize.height)
        commentText.frame = newFrame;
        commentText.scrollEnabled = false;
        */
        //commentText.layer.frame = commentText.frame
        serviceTextView.layer.cornerRadius = 3
        serviceTextView.layer.borderWidth = 0.5
        serviceTextView.layer.borderColor = borderColor.CGColor
        //serviceTextView.textContainer.lineFragmentPadding = 0
        serviceTextView.textContainerInset = UIEdgeInsetsMake(5, 5, 5, 5)
        
        
    }
    
    override func setSelected(selected: Bool, animated: Bool) {
        //super.setSelected(selected, animated: animated)
        
        // Configure the view for the selected state
    }
    
}
