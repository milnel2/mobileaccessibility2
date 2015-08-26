//
//  TimeFunctions.swift
//  DBSC Service App
//
//  Created by Connor Hargus on 8/8/15.
//  Copyright (c) 2015 AppMaker. All rights reserved.
//

import Foundation

func secondsSinceStart() -> Int {
    
    if startDate != nil {
        
        let date = NSDate()
        let formatter = NSDateFormatter()
        formatter.timeStyle = .ShortStyle
        
        var diff:Int = Int(date.timeIntervalSinceDate(startDate!))
        seconds = diff
        
        return seconds
    } else {
        return 0
    }
    
}

