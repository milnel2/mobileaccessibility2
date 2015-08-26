using System;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;
using IronPython.Hosting;
using Microsoft.Scripting.Hosting;
using System.ComponentModel.Composition;
using Microsoft.VisualStudio.Text.Editor;
using Microsoft.VisualStudio.Utilities;
using Microsoft.VisualStudio.TextManager.Interop;
using Microsoft.VisualStudio.Text;
using Microsoft.VisualStudio.Editor;
using IronPython.Compiler;
using IronPython.Compiler.Ast;

namespace UniversityofWashington.VSPackage2
{
    /// <summary>
    /// Interaction logic for MyControl.xaml
    /// </summary>
    public partial class MyControl : UserControl
    {
        public MyControl()
        {
            InitializeComponent();
            System.Diagnostics.Debug.WriteLine("Start");                    

            //test item to add
            TreeViewItem treeItem = new TreeViewItem();
            treeItem.Header = "Test1";
            treeItem.Items.Add(new TreeViewItem() { Header = "TestLabel1" });
            treeView1.Items.Add(treeItem);
        }
         
        /*
         * The method that is called whenever a change is detected by listener. 
         * Updates the TreeView
         * - Note: not good style to have public method like this
         */
        public void PopulateTreeView(PythonAst ast)
        {
            //test item to add
            treeView1.Items.Clear();
            TreeViewItem treeItem = new TreeViewItem();
            treeItem.Header = "Test2";
            treeItem.Items.Add(new TreeViewItem() { Header = "TestLabel2" });
            treeView1.Items.Add(treeItem);

            System.Diagnostics.Debug.WriteLine("Popul tree");

            treeView1.Items.Add(ast);
            treeView1.Items.Refresh();
            treeView1.UpdateLayout();
        }

        /*
         * Set up a listener to detect changes to code.
         */
        [ContentType("code")]
        [Export(typeof(IWpfTextViewCreationListener))]
        [TextViewRole(PredefinedTextViewRoles.Document)]
        internal sealed class TextViewCreationListener : IWpfTextViewCreationListener
        {
            public void TextViewCreated(IWpfTextView textView)
            {
                new TextHandler(textView);
            }
        }
    }
}