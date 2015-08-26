using IronPython.Compiler;
using IronPython.Compiler.Ast;
using IronPython.Hosting;
using Microsoft.Scripting;
using Microsoft.Scripting.Hosting;
using Microsoft.Scripting.Hosting.Providers;
using Microsoft.Scripting.Runtime;
using Microsoft.VisualStudio.Text;
using Microsoft.VisualStudio.Text.Editor;
using System;
using System.Runtime.CompilerServices;
using IronPython.Runtime;
using IronPython.Runtime.Operations;
using IronPython.Runtime.Types;
using IronPython.Runtime.Exceptions;
using Microsoft.Scripting.Utils;
using IronPython;
using System.Windows.Controls;

namespace UniversityofWashington.VSPackage2
{
    /*
     * This class detects changes to the text editor, and only if there are changes does it 
     * retrieve a new ast parser for the text.
     */
    class TextHandler : MyControl
    {
        private IWpfTextView _view;
        private bool _isChangingText;

        
        public TextHandler(IWpfTextView view)
        {
            System.Diagnostics.Debug.WriteLine("instantiate");
            _view = view;
            _view.TextBuffer.Changed += new EventHandler<TextContentChangedEventArgs>(TextBuffer_Changed);
            _view.TextBuffer.PostChanged += new EventHandler(TextBuffer_PostChanged);
        }

        /*
         * Change detected
         */
        private void TextBuffer_Changed(object sender, TextContentChangedEventArgs e)
        {
            System.Diagnostics.Debug.WriteLine("text-changed");
            if (!_isChangingText)
            {
                _isChangingText = true;
                if (e.Changes != null)
                {
                    getCode();
                }
            }
        }

        /*
         * Reset after change detected
         */
        private void TextBuffer_PostChanged(object sender, EventArgs e)
        {
            System.Diagnostics.Debug.WriteLine("post-changed");
            _isChangingText = false;
        }

        /*
         * Get file from Text Editor
         * Make sure filepath ends with '.py'
         */
        private void getCode()
        {
            System.Diagnostics.Debug.WriteLine("get code");
            ITextDocument document;
            _view.TextDataModel.DocumentBuffer.Properties.TryGetProperty(typeof(ITextDocument), out document);
            string text = document.FilePath;
            if (text.EndsWith(".py", StringComparison.OrdinalIgnoreCase))
            {
                getParser(text);
            }
        }

        /*
         * Gets the Python ast parser from the text
         */
        private void getParser(string newText)
        {
            System.Diagnostics.Debug.WriteLine("get Parser");
            ScriptEngine engine = Python.CreateEngine();

            ScriptSource script = engine.CreateScriptSourceFromFile(newText, System.Text.Encoding.Default, SourceCodeKind.AutoDetect);
            LanguageContext pylc = HostingHelpers.GetLanguageContext(engine);
            SourceUnit sci = HostingHelpers.GetSourceUnit(script);
            CompilerContext com = new CompilerContext(sci, pylc.GetCompilerOptions(), ErrorSink.Default);

            Parser parser = Parser.CreateParser(com, (PythonOptions)pylc.Options);
            PythonAst ast = parser.ParseFile(true);

            PopulateTreeView(ast);
        }
    }
}
