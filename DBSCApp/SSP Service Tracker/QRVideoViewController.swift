
//
//  QRVideoViewController.swift
//  SSP Service Tracker
//
//  Created by Connor Hargus on 7/9/15.
//  Copyright (c) 2015 AppMaker. All rights reserved.
//

import UIKit
import AVFoundation

class QRVideoViewController: UIViewController, AVCaptureMetadataOutputObjectsDelegate {

    var captureSession:AVCaptureSession?
    var videoPreviewLayer:AVCaptureVideoPreviewLayer?
    var qrCodeFrameView:UIView?
    var timer = NSTimer()
    
    
    @IBOutlet weak var messageLabel: UILabel!
    
    var QRArray:[String] = [""]
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
    }
    
    override func viewWillAppear(animated: Bool) {
        
        // Get an instance of the AVCaptureDevice class to initialize a device object and provide the video as the media type parameter.
        let captureDevice = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
        
        // Get an instance of the AVCaptureDeviceInput class using the previous device object.
        var error:NSError?
        let input: AnyObject! = AVCaptureDeviceInput.deviceInputWithDevice(captureDevice, error: &error)
        
        if (error != nil) {
            // If any error occurs, simply log the description of it and don't continue any more.
            println("\(error?.localizedDescription)")
            return
        }
        
        // Initialize the captureSession object.
        captureSession = AVCaptureSession()
        // Set the input device on the capture session.
        captureSession?.addInput(input as! AVCaptureInput)
        
        // Initialize a AVCaptureMetadataOutput object and set it as the output device to the capture session.
        let captureMetadataOutput = AVCaptureMetadataOutput()
        captureSession?.addOutput(captureMetadataOutput)
        
        // Set delegate and use the default dispatch queue to execute the call back
        captureMetadataOutput.setMetadataObjectsDelegate(self, queue: dispatch_get_main_queue())
        captureMetadataOutput.metadataObjectTypes = [AVMetadataObjectTypeQRCode]
        
        // Initialize the video preview layer and add it as a sublayer to the viewPreview view's layer.
        videoPreviewLayer = AVCaptureVideoPreviewLayer(session: captureSession)
        videoPreviewLayer?.videoGravity = AVLayerVideoGravityResizeAspectFill
        videoPreviewLayer?.frame = view.layer.bounds
        view.layer.addSublayer(videoPreviewLayer)
        
        // Start video capture.
        captureSession?.startRunning()
        
        // Move the message label to the top view
        view.bringSubviewToFront(messageLabel)
        
        // Initialize QR Code Frame to highlight the QR code
        qrCodeFrameView = UIView()
        qrCodeFrameView?.layer.borderColor = UIColor.greenColor().CGColor
        qrCodeFrameView?.layer.borderWidth = 2
        view.addSubview(qrCodeFrameView!)
        view.bringSubviewToFront(qrCodeFrameView!)
    }
    
    func captureOutput(captureOutput: AVCaptureOutput!, didOutputMetadataObjects metadataObjects: [AnyObject]!, fromConnection connection: AVCaptureConnection!) {
        
        // Check if the metadataObjects array is not nil and it contains at least one object.
        if metadataObjects == nil || metadataObjects.count == 0 {
            qrCodeFrameView?.frame = CGRectZero
            messageLabel.text = "No ID card detected"
            return
        }
        
        // Get the metadata object.
        let metadataObj = metadataObjects[0] as! AVMetadataMachineReadableCodeObject
        
        if metadataObj.type == AVMetadataObjectTypeQRCode {
            // If the found metadata is equal to the QR code metadata then update the status label's text and set the bounds
            let barCodeObject = videoPreviewLayer?.transformedMetadataObjectForMetadataObject(metadataObj as AVMetadataMachineReadableCodeObject) as! AVMetadataMachineReadableCodeObject
            qrCodeFrameView?.frame = barCodeObject.bounds;
            
            if metadataObj.stringValue != nil {
                var stringQR = metadataObj.stringValue
                
                //Parses through different parts of the QR Code, separating by a character
                QRArray = stringQR!.componentsSeparatedByString(", ")
                messageLabel.text = QRArray[0]
                
                //Checks to make sure format was correct (3 elements separated by ", ")
                if QRArray.count == 3 {
                    
                    performSegues()
                    
                } else {
                    
                    messageLabel.text = "Please scan a valid DBSC ID card"
                }
            }
        }
    }
    
    //Performs a different segue depending on whether it's the first or second scan
    func performSegues() {
        timer.invalidate()
        
        var segueString: String = "secondQRRecognized"
        
        if scanNumber == "First" {
            
            QRInfo = QRArray
            client = QRArray[0]
            clientCode = QRArray[1]
            clientEmail = QRArray[2]
            segueString = "QRRecognized"
        }
        
        if QRInfo == QRArray {
            
            //Stops on current screen
            captureSession?.stopRunning()
            
            //Reads VoiceOver to tell that an ID's been found
            UIAccessibilityPostNotification(UIAccessibilityAnnouncementNotification, NSLocalizedString("ID detected", comment: "ID detected"))
            
            //Delays a second so voiceover has time to read
            let delayInSeconds = 0.5
            let waitTime = dispatch_time(DISPATCH_TIME_NOW,
                Int64(delayInSeconds * Double(NSEC_PER_SEC)))
            dispatch_after(waitTime, dispatch_get_main_queue()) {
                self.performSegueWithIdentifier(segueString, sender: self)
                
            }
            
        } else {
            
            messageLabel.text = "Please scan the same ID card"
        }
    
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
