//
//  PrivacyPolicyViewController.swift
//  DBSC Service App
//
//  Created by Connor Hargus on 8/18/15.
//  Copyright (c) 2015 AppMaker. All rights reserved.
//

import UIKit

class PrivacyPolicyViewController: UIViewController {

    @IBOutlet weak var webView: UIWebView!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        let url = NSBundle.mainBundle().URLForResource("privacy-policy/index", withExtension: "html")
        let request = NSURLRequest(URL: url!)
        webView.loadRequest(request)
        
        // Do any additional setup after loading the view.
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
}
