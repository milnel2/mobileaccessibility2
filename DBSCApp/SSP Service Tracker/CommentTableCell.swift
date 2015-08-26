//
//  CommentTableCell.swift
//  SSP Service Tracker
//
//  Created by Connor Hargus on 7/19/15.
//  Copyright (c) 2015 AppMaker. All rights reserved.
//

import UIKit

//Specifies the layout of a custom cell for the comment feed
class CommentTableCell: UITableViewCell {
    
    @IBOutlet weak var commentText: UITextView!
    
    @IBOutlet weak var timeLabel: UILabel!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
        
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
        commentText.layer.cornerRadius = 6
        commentText.layer.borderWidth = 0.5
        commentText.layer.borderColor = borderColor.CGColor
        commentText.textContainer.lineFragmentPadding = 0
        commentText.textContainerInset = UIEdgeInsetsMake(5, 5, 5, 5)
        
    }
    
    override func setSelected(selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }

}
