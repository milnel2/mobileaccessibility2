package edu.washington.cs.scopejumper.views;

import java.util.ArrayList;
import java.util.Stack;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.part.*;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.SWT;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.dom.ASTVisitor;


/**
 * This sample class demonstrates how to plug-in a new
 * workbench view. The view shows data obtained from the
 * model. The sample creates a dummy model on the fly,
 * but a real implementation would connect to the model
 * available either in this or another plug-in (e.g. the workspace).
 * The view is connected to the model using a content provider.
 * <p>
 * The view uses a label provider to define how model
 * objects should be presented in the view. Each
 * view can present the same model objects using
 * different labels and icons, if needed. Alternatively,
 * a single label provider can be shared between views
 * in order to ensure that objects of the same type are
 * presented in the same way everywhere.
 * <p>
 */

public class ScopeTreeView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "edu.washington.cs.scopejumper.views.ScopeTreeView";

	private TreeViewer viewer;
	//private DrillDownAdapter drillDownAdapter;
	private Action action1;
	private Action action2;
	private Action doubleClickAction;

	/*
	 * The content provider class is responsible for
	 * providing objects to the view. It can wrap
	 * existing objects in adapters or simply return
	 * objects as-is. These objects may be sensitive
	 * to the current input of the view, or ignore
	 * it and always show the same content 
	 * (like Task List, for example).
	 */
	 
	class TreeObject implements IAdaptable {
		private String name;
		private int end;
		private int start;
		private TreeParent parent;
		private int childIndex;
		private int endLine;
		
		
		public TreeObject(String name, int start, int end) {
			this.name = name;
			this.end = end;
			this.childIndex =0;
			this.start = start;
			this.endLine = end;
		}
		public TreeObject(String name, int start, int end, int endLine){
			this.name = name;
			this.end = end;
			this.childIndex =0;
			this.start = start;
			this.endLine = endLine;
		}
		public String getName() {
			return name;
		}
		public void setParent(TreeParent parent) {
			this.parent = parent;
		}
		public TreeParent getParent() {
			return parent;
		}
		public int getEnd(){
			return end;
		}
		public void setEnd(int end){
			this.end = end;
		}
		public int getStart(){
			return start;
		}
		public String toString() {
			//String test = start+","+endLine+","+end+" "+getName();
			//return test;
			return getName();
		}
		public Object getAdapter(Class key) {
			return null;
		}
		public void setChildIndex(int n){
			this.childIndex = n;
		}
		public int getChildIndex(){
			return this.childIndex;
		}
		public void setEndLine(int i) {
			this.endLine = i;
		}
		public int getEndLine(){
			return this.endLine;
		}
	}
	
	class TreeParent extends TreeObject {
		private ArrayList children;
		public TreeParent(String name, int start, int end,int endLine) {
			super(name, start, end, endLine);
			children = new ArrayList();
		}
		public void addChild(TreeObject child) {
			children.add(child);
			child.setParent(this);
			//System.out.println(child.getName());
			child.setChildIndex(children.indexOf(child));
		}
		public void removeChild(TreeObject child) {
			children.remove(child);
			child.setParent(null);
			int i =0;
			for(Object kid : children){
				if(kid instanceof TreeObject){
					((TreeObject) kid).childIndex =i; 
				}
				i++;
			}
		}
		public TreeObject [] getChildren() {
			return (TreeObject [])children.toArray(new TreeObject[children.size()]);
		}
		public boolean hasChildren() {
			return children.size()>0;
		}
	}

	class ViewContentProvider implements IStructuredContentProvider, 
										   ITreeContentProvider {
		private TreeParent invisibleRoot;
		private Stack<TreeParent> treeNodes;
		private Stack<TreeObject> treeLeafs;
		private boolean elseIf;
		
		public Object[] getRoot(){
			return getChildren(invisibleRoot);
		}
		
		public Object[] getElements(Object parent) {
			if (parent.equals(getViewSite())) {
				if (invisibleRoot==null) initialize();
				return getChildren(invisibleRoot);
			}
			return getChildren(parent);
		}
		public Object getParent(Object child) {
			if (child instanceof TreeObject) {
				return ((TreeObject)child).getParent();
			}
			return null;
		}
		public Object [] getChildren(Object parent) {
			if (parent instanceof TreeParent) {
				return ((TreeParent)parent).getChildren();
			}
			return new Object[0];
		}
		public boolean hasChildren(Object parent) {
			if (parent instanceof TreeParent)
				return ((TreeParent)parent).hasChildren();
			return false;
		}
/*
 * This code sets up a modelWe will set up a dummy model to initialize tree hierarchy.
 * In a real code, you will connect to a real model and
 * expose its hierarchy.
 */
		
		private void initialize(){
			invisibleRoot = new TreeParent("",0,0,0);
			String str = "";
			elseIf = false;
			IWorkbench bench = PlatformUI.getWorkbench(); 
			if(bench != null){
				IEditorPart part = bench.getActiveWorkbenchWindow().getActivePage().getActiveEditor();
				if(part != null && part.getTitle().contains(".java")){

					ITextEditor editor = (ITextEditor) part.getAdapter(ITextEditor.class);
					IDocumentProvider provider = editor.getDocumentProvider();
					IDocument document = provider.getDocument(part.getEditorInput());
					str = document.get();
				}
			}
			ASTParser parser = ASTParser.newParser(AST.JLS3);
			parser.setSource(str.toCharArray());
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			treeLeafs = new Stack<TreeObject>();
			treeNodes = new Stack<TreeParent>();
			treeNodes.add(invisibleRoot);
			final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
	 
			cu.accept(new ASTVisitor() {
	 
				
	 
				public boolean visit(ForStatement node) {
					if(!treeLeafs.isEmpty()){
						TreeObject temp = treeLeafs.pop();
						temp.setEnd(node.getStartPosition());
						temp.setEndLine(node.getStartPosition());
					}
					TreeParent par = treeNodes.peek();
					String temp = node.initializers().toString();
					String name = "for("+temp.substring(1,temp.length()-1) +"; ";
					temp = node.getExpression().toString();
					name = name+temp.substring(0, temp.length()-1)+"; ";
					temp = node.updaters().toString();
					name = name + temp.substring(1, temp.length()-1)+")";
					TreeParent add = new TreeParent(name,node.getStartPosition(), node.getStartPosition()+node.getLength(),node.getBody().getStartPosition());
					par.addChild(add);
					treeNodes.push(add);
					return true; 
				}
				
				public void endVisit(ForStatement node){
					if(!treeLeafs.isEmpty()){
						TreeObject temp = treeLeafs.pop();
						temp.setEnd(node.getStartPosition()+node.getLength()-1);
						temp.setEndLine(node.getStartPosition()+node.getLength()-1);
					}
					treeNodes.pop();
				}
	 
				public boolean visit(TypeDeclaration node) {
					if(!treeLeafs.isEmpty()){
						TreeObject temp = treeLeafs.pop();
						temp.setEnd(node.getStartPosition());
						temp.setEndLine(node.getStartPosition());
					}
					TreeParent par = treeNodes.peek();
					String name = "";
					String temp = "";
					if(node.isInterface()){
						name = " interface";
					}
					else{
						name = " class";
					}
					name = name +node.getName().toString();
					if(node.getSuperclassType() != null){
						name = name+" extends " +node.getSuperclassType().toString();
					}
					
					if(node.superInterfaceTypes().size()!=0){
						temp = node.superInterfaceTypes().toString();
						name = name+" implements "+temp.substring(1, temp.length()-1);
					}
					if(node.modifiers().size()!=0){
						temp = node.modifiers().toString();
						name = name+ " " +temp.substring(1, temp.length()-1);
					}
					if(node.getJavadoc()!=null){
						name = name + " Comments: "+node.getJavadoc().toString().replace('\n', ' ');
					}
					
					int loc = node.toString().indexOf('{');
					TreeParent add = new TreeParent(name,node.getStartPosition(), node.getStartPosition()+node.getLength(),node.getStartPosition()+loc);
					par.addChild(add);
					treeNodes.push(add);
					return true;
				}
				public void endVisit(TypeDeclaration node){
					if(!treeLeafs.isEmpty()){
						TreeObject temp = treeLeafs.pop();
						temp.setEnd(node.getStartPosition()+node.getLength()-1);
						temp.setEndLine(node.getStartPosition()+node.getLength()-1);
					}
					treeNodes.pop();
				}
				
				public boolean visit(SwitchStatement node) {
					if(!treeLeafs.isEmpty()){
						TreeObject temp = treeLeafs.pop();
						temp.setEnd(node.getStartPosition());
						temp.setEndLine(node.getStartPosition());
					}
					TreeParent par = treeNodes.peek();
					String name = "switch( "+node.getExpression()+")";
					int loc = node.toString().indexOf('{');
					TreeParent add = new TreeParent(name,node.getStartPosition(), node.getStartPosition()+node.getLength(),loc+node.getStartPosition());
					par.addChild(add);
					treeNodes.push(add);
					return true;
				}
				
				public void endVisit(SwitchStatement node){
					if(!treeLeafs.isEmpty()){
						TreeObject temp = treeLeafs.pop();
						temp.setEnd(node.getStartPosition()+node.getLength()-1);
						temp.setEndLine(node.getStartPosition()+node.getLength()-1);
					}
					while(treeNodes.peek().toString().indexOf("case")!=-1 || treeNodes.peek().toString().indexOf("default")!=-1){
						treeNodes.pop();
					}
					treeNodes.pop();
				}
				
				public boolean visit(SwitchCase node) {
					if(!treeLeafs.isEmpty()){
						TreeObject temp = treeLeafs.pop();
						temp.setEnd(node.getStartPosition());
						temp.setEndLine(node.getStartPosition());
					}
					while(treeNodes.peek().toString().indexOf("case")!=-1 || treeNodes.peek().toString().indexOf("default")!=-1){
						treeNodes.pop();
					}
					TreeParent par = treeNodes.peek();
					String name = "";
					if(node.isDefault()){
						name = "default";
					}
					else{
						name = "case "+node.getExpression();
					}
					int loc = node.toString().indexOf(':');
					TreeParent add = new TreeParent(name,node.getStartPosition(), node.getStartPosition()+node.getLength(),node.getStartPosition()+loc);
					par.addChild(add);
					treeNodes.push(add);
					return true;
				}
				
				public void endVisit(SwitchCase node){
					if(!treeLeafs.isEmpty()){
						TreeObject temp = treeLeafs.pop();
						temp.setEnd(node.getStartPosition()+node.getLength()-1);
						temp.setEndLine(node.getStartPosition()+node.getLength()-1);
					}
					//TreeParent temp = treeNodes.pop();
					//System.out.println(temp.toString());
				}
				
				
				public boolean visit(WhileStatement node) {
					if(!treeLeafs.isEmpty()){
						TreeObject temp = treeLeafs.pop();
						temp.setEnd(node.getStartPosition());
						temp.setEndLine(node.getStartPosition());
					}
					TreeParent par = treeNodes.peek();
					String name = "while( "+node.getExpression()+")";
					TreeParent add = new TreeParent(name,node.getStartPosition(), node.getStartPosition()+node.getLength(),node.getBody().getStartPosition());
					par.addChild(add);
					treeNodes.push(add);
					return true;
				}
				
				public void endVisit(WhileStatement node){
					if(!treeLeafs.isEmpty()){
						TreeObject temp = treeLeafs.pop();
						temp.setEnd(node.getStartPosition()+node.getLength()-1);
						temp.setEndLine(node.getStartPosition()+node.getLength()-1);
					}
					treeNodes.pop();
				}
				
				public boolean visit(IfStatement node) {
					if(!treeLeafs.isEmpty()){
						TreeObject temp = treeLeafs.pop();
						temp.setEnd(node.getStartPosition());
						temp.setEndLine(node.getStartPosition());
					}
					TreeParent par = treeNodes.peek();
					String name = "if("+node.getExpression().toString()+")";
					if(node.getParent().getClass() == node.getClass()){
						name = "else "+name;
					}
					TreeParent add = new TreeParent(name,node.getStartPosition(), node.getThenStatement().getStartPosition()+node.getThenStatement().getLength(),node.getThenStatement().getStartPosition());
					par.addChild(add);
					if(node.getElseStatement()!=null){
						if(node.getElseStatement().getClass() != node.getClass()){
							TreeParent el = new TreeParent("else",node.getThenStatement().getStartPosition()+node.getThenStatement().getLength(),node.getElseStatement().getStartPosition()+node.getElseStatement().getLength(),node.getElseStatement().getStartPosition());
							par.addChild(el);
							treeNodes.push(el);
						}
					}
					treeNodes.push(add);					
					return true;
					
				}
				public void endVisit(IfStatement node){
					if(!treeLeafs.isEmpty()){
						TreeObject temp = treeLeafs.pop();
						temp.setEnd(node.getStartPosition()+node.getLength()-1);
						temp.setEndLine(node.getStartPosition()+node.getLength()-1);
					}
					//treeNodes.pop();
				}
				
				public boolean visit(DoStatement node) {
					if(!treeLeafs.isEmpty()){
						TreeObject temp = treeLeafs.pop();
						temp.setEnd(node.getStartPosition());
						temp.setEndLine(node.getStartPosition());
					}
					TreeParent par = treeNodes.peek();
					String name = "do while("+node.getExpression().toString()+")";
					TreeParent add = new TreeParent(name,node.getStartPosition(), node.getStartPosition()+node.getLength(),node.getBody().getStartPosition());
					par.addChild(add);
					treeNodes.push(add);
					return true;
				}
				public void endVisit(DoStatement node){
					if(!treeLeafs.isEmpty()){
						TreeObject temp = treeLeafs.pop();
						temp.setEnd(node.getStartPosition()+node.getLength()-1);
						temp.setEndLine(node.getStartPosition()+node.getLength()-1);
					}
					treeNodes.pop();
				}
				
				public boolean visit(MethodDeclaration node) {
					if(!treeLeafs.isEmpty()){
						TreeObject temp = treeLeafs.pop();
						temp.setEnd(node.getStartPosition());
						temp.setEndLine(node.getStartPosition());
					}
					TreeParent par = treeNodes.peek();
					String temp;
					String name = node.getName().toString() + " Input: ";
					if(node.parameters().size() != 0){
						temp = node.parameters().toString();
						name = name+temp.substring(1, temp.length()-1);
					}
					else{
						name = name+"none";
					}
					if(!node.isConstructor()){
						name = name +"; Return Type "+node.getReturnType2().toString();
					}
					if(node.modifiers().size()!=0){
						temp = node.modifiers().toString();
						name = name+"; Modifiers: "+temp.substring(1, temp.length()-1);
					}
					if(node.getJavadoc()!=null){
						name = name + "; Comment: "+node.getJavadoc().toString().replace('\n', ' ');
					}
					TreeParent add = new TreeParent(name,node.getStartPosition(), node.getStartPosition()+node.getLength(),node.getBody().getStartPosition());
					par.addChild(add);
					treeNodes.push(add);
					return true;
				}
				public void endVisit(MethodDeclaration node){
					if(!treeLeafs.isEmpty()){
						TreeObject temp = treeLeafs.pop();
						temp.setEnd(node.getStartPosition()+node.getLength()-1);
						temp.setEndLine(node.getStartPosition()+node.getLength()-1);
					}
					treeNodes.pop();
				}
				
				public boolean visit(AnonymousClassDeclaration node) {
					if(!treeLeafs.isEmpty()){
						TreeObject temp = treeLeafs.pop();
						temp.setEnd(node.getStartPosition());
						temp.setEndLine(node.getStartPosition());
					}
					TreeParent par = treeNodes.peek();
					String name = "Anonymous Class Declaration";
							//node.resolveBinding().getName() + "anonymous declaration";
					int loc = node.toString().indexOf('{');
					TreeParent add = new TreeParent(name,node.getStartPosition(), node.getStartPosition()+node.getLength(),node.getStartPosition()+loc);
					par.addChild(add);
					treeNodes.push(add);
					return true;
				}
				public void endVisit(AnonymousClassDeclaration node){
					if(!treeLeafs.isEmpty()){
						TreeObject temp = treeLeafs.pop();
						temp.setEnd(node.getStartPosition()+node.getLength()-1);
						temp.setEndLine(node.getStartPosition()+node.getLength()-1);
					}
					treeNodes.pop();
				}
				
				public boolean visit(EnumDeclaration node) {
					if(!treeLeafs.isEmpty()){
						TreeObject temp = treeLeafs.pop();
						temp.setEnd(node.getStartPosition());
						temp.setEndLine(node.getStartPosition());
					}
					TreeParent par = treeNodes.peek();
					String name = node.getName().toString();
					if(node.superInterfaceTypes()!=null){
						name = name+" implements "+node.superInterfaceTypes().toString();
					}
					if(node.modifiers()!= null){
						name = name+" Modifiers: "+node.modifiers().toString();
					}
					name = name + " enum";
					int loc = node.toString().indexOf('{');
					TreeParent add = new TreeParent(name,node.getStartPosition(), node.getStartPosition()+node.getLength(),node.getStartPosition()+loc);
					par.addChild(add);
					treeNodes.push(add);
					return true;
				}
				public void endVisit(EnumDeclaration node){
					if(!treeLeafs.isEmpty()){
						TreeObject temp = treeLeafs.pop();
						temp.setEnd(node.getStartPosition()+node.getLength()-1);
						temp.setEndLine(node.getStartPosition()+node.getLength()-1);
					}
					treeNodes.pop();
				}
				
				public boolean visit(LabeledStatement node){
					if(!treeLeafs.isEmpty()){
						TreeObject temp = treeLeafs.pop();
						temp.setEnd(node.getStartPosition());
						temp.setEndLine(node.getStartPosition());
					}
					TreeParent par = treeNodes.peek();
					String name = node.getLabel().toString();
					TreeParent add = new TreeParent(name,node.getStartPosition(), node.getStartPosition()+node.getLength(),node.getBody().getStartPosition());
					par.addChild(add);
					treeNodes.push(add);
					return true;
				}
				public void endVisit(LabeledStatement node){
					if(!treeLeafs.isEmpty()){
						TreeObject temp = treeLeafs.pop();
						temp.setEnd(node.getStartPosition()+node.getLength()-1);
						temp.setEndLine(node.getStartPosition()+node.getLength()-1);
					}
				}
											
				public boolean visit(EnhancedForStatement node) {
					if(!treeLeafs.isEmpty()){
						TreeObject temp = treeLeafs.pop();
						temp.setEnd(node.getStartPosition());
						temp.setEndLine(node.getStartPosition());
					}
					TreeParent par = treeNodes.peek();
					String name = "for( "+node.getParameter().toString()+" : "+node.getExpression().toString()+")";
					TreeParent add = new TreeParent(name,node.getStartPosition(), node.getStartPosition()+node.getLength(),node.getBody().getStartPosition());
					par.addChild(add);
					treeNodes.push(add);
					return true;
				}
				public void endVisit(EnhancedForStatement node){
					if(!treeLeafs.isEmpty()){
						TreeObject temp = treeLeafs.pop();
						temp.setEnd(node.getStartPosition()+node.getLength()-1);
						temp.setEndLine(node.getStartPosition()+node.getLength()-1);
					}
					treeNodes.pop();
				}
				
				public boolean visit(TryStatement node) {
					if(!treeLeafs.isEmpty()){
						TreeObject temp = treeLeafs.pop();
						temp.setEnd(node.getStartPosition());
						temp.setEndLine(node.getStartPosition());
					}
					int count = 0;
					TreeParent par = treeNodes.peek();
					int loc = node.getBody().getLength()+node.getBody().getStartPosition();
					TreeParent add = new TreeParent("try",node.getStartPosition(), node.getBody().getStartPosition(),loc);					
					par.addChild(add);
					count++;
					for(int i = 0; i<node.catchClauses().size();i++){
						String name = "catch( "+((CatchClause)node.catchClauses().get(i)).getException()+")";
						loc = ((CatchClause)node.catchClauses().get(i)).getStartPosition()+((CatchClause)node.catchClauses().get(i)).getLength();
						add = new TreeParent(name,((CatchClause)node.catchClauses().get(i)).getStartPosition(), ((CatchClause)node.catchClauses().get(i)).getBody().getStartPosition(),loc);
						par.addChild(add);	
						count++;
					}
					
					if(node.getFinally()!=null){
						add = new TreeParent("finally",loc, node.getFinally().getStartPosition(),node.getFinally().getStartPosition()+node.getFinally().getLength());
						par.addChild(add);
						count++;
					}
					
					/*
					 * This needs to be redone only look at ones we care about
					 * if 10 nodes and 4 are apart of stop at 6
					 */
					TreeObject[] theNodes = par.getChildren();
					for(int i = theNodes.length-1; i>=(theNodes.length-count); i--){
						treeNodes.push((TreeParent)theNodes[i]);
					}
					return true;
				}
				
				public boolean visit(Statement node){
					
					
					if(treeLeafs.isEmpty()){
						TreeObject leaf = new TreeObject("Code Section",node.getStartPosition(),0);
						TreeParent addTo = treeNodes.peek();
						addTo.addChild(leaf);
						treeLeafs.push(leaf);
					}
					
					return true;
				}
				
				public void endVisit(Statement node){
					statementEndVisit(node);
				}
				
				public boolean visit(AssertStatement node){
					
					if(treeLeafs.isEmpty()){
						TreeObject leaf = new TreeObject("Code Section",node.getStartPosition(),0);
						TreeParent addTo = treeNodes.peek();
						addTo.addChild(leaf);
						treeLeafs.push(leaf);
					}
					return true;
				}
				public void endVisit(AssertStatement node){
					statementEndVisit(node);
				}
				
				public boolean visit(BreakStatement node){
					
					if(treeLeafs.isEmpty()){
						TreeObject leaf = new TreeObject("Code Section",node.getStartPosition(),0);
						TreeParent addTo = treeNodes.peek();
						addTo.addChild(leaf);
						treeLeafs.push(leaf);
					}
					return true;
				}
				public void endVisit(BreakStatement node){
					statementEndVisit(node);
				}
				
				public boolean visit(ContinueStatement node){
					
					if(treeLeafs.isEmpty()){
						TreeObject leaf = new TreeObject("Code Section",node.getStartPosition(),0);
						TreeParent addTo = treeNodes.peek();
						addTo.addChild(leaf);
						treeLeafs.push(leaf);
					}
					return true;
				}
				public void endVisit(ContinueStatement node){
					statementEndVisit(node);
				}
				
				
				
				public boolean visit(EmptyStatement node){
					
					if(treeLeafs.isEmpty()){
						TreeObject leaf = new TreeObject("Code Section",node.getStartPosition(),0);
						TreeParent addTo = treeNodes.peek();
						addTo.addChild(leaf);
						treeLeafs.push(leaf);
					}
					return true;
				}
				public void endVisit(EmptyStatement node){
					statementEndVisit(node);
				}
				
				public boolean visit(ExpressionStatement node){
					
					if(treeLeafs.isEmpty()){
						TreeObject leaf = new TreeObject("Code Section",node.getStartPosition(),0);
						TreeParent addTo = treeNodes.peek();
						addTo.addChild(leaf);
						treeLeafs.push(leaf);
					}
					return true;
				}
				public void endVisit(ExpressionStatement node){
					statementEndVisit(node);
				}
				
				public boolean visit(ReturnStatement node){
					
					if(treeLeafs.isEmpty()){
						TreeObject leaf = new TreeObject("Code Section",node.getStartPosition(),0);
						TreeParent addTo = treeNodes.peek();
						addTo.addChild(leaf);
						treeLeafs.push(leaf);
					}
					return true;
				}		
				public void endVisit(ReturnStatement node){
					statementEndVisit(node);
				}
				
				public boolean visit(ThrowStatement node){
					
					if(treeLeafs.isEmpty()){
						TreeObject leaf = new TreeObject("Code Section",node.getStartPosition(),0);
						TreeParent addTo = treeNodes.peek();
						addTo.addChild(leaf);
						treeLeafs.push(leaf);
					}
					return true;
				}			
				public void endVisit(ThrowStatement node){
					statementEndVisit(node);
				}
				
				public boolean visit(Block node){
					//System.out.println("testing");
					return true;
				}
				public void endVisit(Block node){
					statementEndVisit(node);
				}
				
				public boolean visit(Assignment node){
					if(treeLeafs.isEmpty()){
						TreeObject leaf = new TreeObject("Code Section",node.getStartPosition(),0);
						TreeParent addTo = treeNodes.peek();
						addTo.addChild(leaf);
						treeLeafs.push(leaf);
					}
					return false;
				}
				public boolean visit(EnumConstantDeclaration node){
					if(treeLeafs.isEmpty()){
						TreeObject leaf = new TreeObject("Code Section",node.getStartPosition(),0);
						TreeParent addTo = treeNodes.peek();
						addTo.addChild(leaf);
						treeLeafs.push(leaf);
					}
					return true;
				}
				public boolean visit(FieldDeclaration node){
					if(treeLeafs.isEmpty()){
						TreeObject leaf = new TreeObject("Code Section",node.getStartPosition(),0);
						TreeParent addTo = treeNodes.peek();
						addTo.addChild(leaf);
						treeLeafs.push(leaf);
					}
					return true;
				}
				
				public boolean visit(ImportDeclaration node){
					if(treeLeafs.isEmpty()){
						TreeObject leaf = new TreeObject("Code Section",node.getStartPosition(),0);
						TreeParent addTo = treeNodes.peek();
						addTo.addChild(leaf);
						treeLeafs.push(leaf);
					}
					return true;
				}
				
				
				
				public boolean visit(PackageDeclaration node){
					if(treeLeafs.isEmpty()){
						TreeObject leaf = new TreeObject("Code Section",node.getStartPosition(),0);
						TreeParent addTo = treeNodes.peek();
						addTo.addChild(leaf);
						treeLeafs.push(leaf);
					}
					return true;
				}
				
				public boolean visit(VariableDeclarationStatement node){
					if(treeLeafs.isEmpty()){
						TreeObject leaf = new TreeObject("Code Section",node.getStartPosition(),0);
						TreeParent addTo = treeNodes.peek();
						addTo.addChild(leaf);
						treeLeafs.push(leaf);
					}
					return true;
				}
				
				public void endVisit(VariableDeclarationStatement node){
					statementEndVisit(node);
				}
				
				public boolean visit(BlockComment node){
					TreeObject leaf = new TreeObject("~~~~COMMENT~~~~",node.getStartPosition(),0);
					TreeParent addTo = treeNodes.peek();
					addTo.addChild(leaf);
					return true;
				}
				
				
				public boolean visit(SuperConstructorInvocation node){
					if(treeLeafs.isEmpty()){
						TreeObject leaf = new TreeObject("Code Section",node.getStartPosition(),0);
						TreeParent addTo = treeNodes.peek();
						addTo.addChild(leaf);
						treeLeafs.push(leaf);
					}
					return true;
				}
				public void endVisit(SuperConstructorInvocation node){
					statementEndVisit(node);
				}
				
				public void statementEndVisit(Statement node){
					if(node.getParent() instanceof IfStatement || node.getParent() instanceof TryStatement || node.getParent() instanceof CatchClause){
						treeNodes.pop();
						if(!treeLeafs.isEmpty()){
							TreeObject temp = treeLeafs.pop();
							temp.setEnd(node.getStartPosition()+node.getLength()-1);
							temp.setEndLine(node.getStartPosition()+node.getLength()-1);
						}
					}
				}
				
				
			});
	 
		}
		
		@Override
		public void dispose() {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// TODO Auto-generated method stub
			
		}
	}
	class ViewLabelProvider extends LabelProvider {

		public String getText(Object obj) {
			return obj.toString();
		}
		public Image getImage(Object obj) {
			String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
			if (obj instanceof TreeParent)
			   imageKey = ISharedImages.IMG_OBJ_FOLDER;
			return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
		}
	}
	class NameSorter extends ViewerSorter {
	}

	/**
	 * The constructor.
	 */
	public ScopeTreeView() {
		
	}
	

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		//TODO drillDownAdapter implements web style navigation--should add in navigation stuff here
		//drillDownAdapter = new DrillDownAdapter(viewer);
		ViewContentProvider vcp = new ViewContentProvider();
		viewer.setContentProvider(vcp);
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setInput(getViewSite());
		viewer.expandAll();
		final Class<? extends ScopeTreeView>  compare = this.getClass();
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        //IWorkbenchPart active = page.getActivePart();
        IPartListener pl = new IPartListener(){
        	 
        	//Listener so that when enter StructTree View, will go to the location
        	//in the tree corresponding to the cursor location
        	@Override
        	public void partActivated(IWorkbenchPart part) {
        		//Does not update cursor loc if not a java file
        		//System.out.println("Where");
        		IWorkbench bench = PlatformUI.getWorkbench();
        		if(bench != null){
        			//System.out.println("Did");
        			IEditorPart part2 = bench.getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        			if(part !=null){
        				//System.out.println("It");
        				if(part.getClass().equals(compare) && part2.getTitle().contains(".java")){
        					//DO SEARCH
        					//System.out.println("Stop");
        					//System.out.println("start");
        					MoveToCursor();
        					//System.out.println("Done");
        				}
        			}
        		}
        	}
        	@Override
            public void partBroughtToTop(IWorkbenchPart part) {
                    // TODO Auto-generated method stub
                   
            }

            @Override
            public void partClosed(IWorkbenchPart part) {
                    // TODO Auto-generated method stub
                   
            }

            @Override
            public void partDeactivated(IWorkbenchPart part) {
                    // TODO Auto-generated method stub
                    //showMessage("part deactivated");
            }

            @Override
            public void partOpened(IWorkbenchPart part) {
            	// TODO Auto-generated method stub

            }

        };
        page.addPartListener(pl);

        // Create the help context id for the viewer's control
        PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "edu.washington.cs.scopejumper.viewer");
        makeActions();
        hookContextMenu();
        hookDoubleClickAction();
        contributeToActionBars();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				ScopeTreeView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(action1);
		manager.add(new Separator());
		manager.add(action2);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(action1);
		manager.add(action2);
		manager.add(new Separator());
		//drillDownAdapter.addNavigationActions(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(action1);
		manager.add(action2);
		manager.add(new Separator());
		//drillDownAdapter.addNavigationActions(manager);
	}

	private void makeActions() {
		action1 = new Action() {
			public void run() {
				showMessage("Action 1 executed");
			}
		};
		action1.setText("Action 1");
		action1.setToolTipText("Action 1 tooltip");
		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
			getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		
		action2 = new Action() {
			public void run() {
				showMessage("Action 2 executed");
				switchToEditingMode();
			}
		};
		action2.setText("Action 2");
		action2.setToolTipText("Action 2 tooltip");
		action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		doubleClickAction = new Action() {
			public void run() {
				//ISelection selection = viewer.getSelection();
				//Object obj = ((IStructuredSelection)selection).getFirstElement();
				//showMessage("Double-click detected on "+obj.toString());
				switchToEditingMode();
			}
		};
	}
	
	private void switchToEditingMode(){
		ISelection selection = viewer.getSelection();
		Object obj = ((IStructuredSelection)selection).getFirstElement();
		if (obj instanceof TreeObject){
			int loc = ((TreeObject) obj).getStart();
			//showMessage("trying to jump to "+ loc);
			
		/*String temp = "";		
		if(part != null){
			AbstractTextEditor compEditor = (AbstractTextEditor)part;
        	IEditorInput input = compEditor.getEditorInput();
        	IDocumentProvider provider = compEditor.getDocumentProvider();
        	IDocument document = provider.getDocument(input);
        	temp = document.get();
		}*/
			IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
			part.setFocus();
			((ITextEditor)part).selectAndReveal(loc,0);
		}
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
		viewer.getControl().addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent event) {
				event.doit = true;
				/*if(getNavigationRoot() == null || getCurrentNode() == null) {
					if(hasSelection(getTree())) {
						setNavigationRoot(DestructTreeItemArray(getTree().getSelection()));
						setCurrentNode(DestructTreeItemArray(getTree().getSelection()));	
					} else {
						setNavigationRoot(getTree().getTopItem());
						setCurrentNode(getTree().getTopItem());
					}	
				}*/
				
				if(event.keyCode == SWT.ARROW_LEFT) {
					MoveToParent();
				} else if(event.keyCode == SWT.ARROW_RIGHT) {
					MoveToChild();
				} else if(event.keyCode == SWT.ARROW_UP) {
					MoveToPreviousChild();
				} else if(event.keyCode == SWT.ARROW_DOWN) {
					MoveToNextChild();					
				} else if(event.character == 'e' || event.character == 'E') {
					switchToEditingMode();
				}
				else if(event.character == 'u' || event.character == 'U'){
					updateTree();
				}
				else if(event.character == 'c' || event.character == 'C'){
					MoveToCursor();
				}
				else if(event.character == 't' || event.character == 'T'){
					MoveToTop();
				}
				event.doit = false;
			}
			
			public void keyReleased(KeyEvent event) {
				//handleKeyReleased(event);
			}
		});
	}
	
	private void MoveToTop(){
		ViewContentProvider vcp2 = (ViewContentProvider) viewer.getContentProvider();
		TreeObject firstChild = (TreeObject) vcp2.getRoot()[0];
		viewer.setSelection(new StructuredSelection(firstChild));
	}
	private void updateTree(){
		ViewContentProvider vcp = new ViewContentProvider();
		viewer.setContentProvider(vcp);
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setInput(getViewSite());
		viewer.expandAll();
		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "edu.washington.cs.scopejumper.viewer");
	}
	private void MoveToChild(){
		ISelection selection = viewer.getSelection();
		Object obj = ((IStructuredSelection)selection).getFirstElement();
		if (obj instanceof TreeParent){
			TreeParent parentObj = (TreeParent)obj;
			if(parentObj.hasChildren()){
				TreeObject firstChild = parentObj.getChildren()[0];
				//showMessage("Child node "+ firstChild.toString());
				viewer.setSelection(new StructuredSelection(firstChild));
			}
		}
	}
	
	private void MoveToCursor(){
		ViewContentProvider vcp2 = (ViewContentProvider) viewer.getContentProvider();
		TreeObject[] nodes = (TreeObject[]) vcp2.getRoot();
		boolean found = false;
		boolean change = false;
		
		IEditorPart part2 = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		int cursorLoc = 0;
        if(part2 != null){
                ITextSelection textSelection = (ITextSelection) part2.getSite().getSelectionProvider().getSelection();
                cursorLoc = textSelection.getOffset();
                if(cursorLoc == -1)
                {
                	cursorLoc = 0;
                }
        }
		
		if(nodes.length > 0){
			TreeObject select = nodes[0];
			while(!found){
				change = false;
				for(int i = 0; i<nodes.length && !change && !found; i++){
					//System.out.println(nodes[i].getEndLine() +" "+nodes[i].getEnd());
					if(nodes[i].getEndLine()>cursorLoc){
						found = true;
						select = (TreeObject) nodes[i];
						break;
					}
					else if(nodes[i].getEnd()>cursorLoc){
						if(!vcp2.hasChildren(nodes[i])){
							select = nodes[i];
							found = true;
							//System.out.println("found");
							break;
						}
						else{
							nodes = (TreeObject[]) vcp2.getChildren(nodes[i]);
							change = true;
							//System.out.println("changed");
							break;
						}
					}
				}
				if(!found && !change){
					select = (TreeObject) vcp2.getParent(nodes[0]);
					//Not in current tree; select first node and break
					break;
				}
			}
			//System.out.println("cursor loc: "+cursorLoc+" "+select.getStart()+" "+select.getEndLine()+" "+select.getEnd());
			viewer.setSelection(new StructuredSelection(select));
		}
	}
	
	private void MoveToNextChild(){
		ISelection selection = viewer.getSelection();
		Object obj = ((IStructuredSelection)selection).getFirstElement();
		if (obj instanceof TreeObject){
			TreeObject treeObj = (TreeObject)obj;
			TreeParent parent = treeObj.getParent();
			if(parent!=null){
				int index = treeObj.getChildIndex();
				if(parent.getChildren().length-1 >index){
					//showMessage("Current node index "+ index + " parentLength " +parent.getChildren().length);
					viewer.setSelection(new StructuredSelection(parent.getChildren()[index+1]));
					//showMessage("New selection "+ parent.getChildren()[index+1].toString());
				}
			}
		}
	}
	
	private void MoveToPreviousChild(){
		ISelection selection = viewer.getSelection();
		Object obj = ((IStructuredSelection)selection).getFirstElement();
		if (obj instanceof TreeObject){
			TreeObject treeObj = (TreeObject)obj;
			TreeParent parent = treeObj.getParent();
			if(parent!=null){
				int index = treeObj.getChildIndex();
				if(index>0){
					//showMessage("Current node index "+ index + " parentLength " +parent.getChildren().length);
					viewer.setSelection(new StructuredSelection(parent.getChildren()[index-1]));
					//showMessage("New selection "+ parent.getChildren()[index-1].toString());
				}
			}
		}
	}
	
	private void MoveToParent(){
		ISelection selection = viewer.getSelection();
		Object obj = ((IStructuredSelection)selection).getFirstElement();
		if (obj instanceof TreeObject){
			TreeObject treeObj = (TreeObject)obj;
			if(treeObj.getParent().getParent()!=null){
				viewer.setSelection(new StructuredSelection(treeObj.getParent()));
			}
		}
	}

	
	private void showMessage(String message) {
		MessageDialog.openInformation(
			viewer.getControl().getShell(),
			"ScopeTree View",
			message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	
}