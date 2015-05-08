package android_talking_software.applications.calculator;
public class Calculator 
{
	private static int pos, before, after, amount;
	private static String replacement;
	private static StringBuffer expression;
	private static String[] operators = {"^", "*", "/", "+", "-"};

	public static void calculate(StringBuffer input)
	{
		expression = new StringBuffer(input+" ");
		if (isValid()) calculate(0, expression.length()-1);
		else expression = new StringBuffer("Error");
		input.replace(0, input.length(), expression.toString());
	}

	private static int calculate(int start, int end)
	{
		if (!expression.substring(start, end).matches("\\(-[a-zA-Z0-9\\.]+\\)"))
		{
			if (trimParenthesis(start, end))
				end-= 2;
			if (expression.charAt(start) == '-') start++;
			if (!expression.substring(start, end).matches(".*\\(.*[0-9\\.]+[\\^\\*\\/\\+][0-9\\.]+.*\\).*"))
			{
				for (String operator:operators)
				{
					end -= findAndEvaluate(start, end, operator);
					if (expression.substring(start, end).matches("\\(-[a-zA-Z0-9\\.]+\\)")) break;
				}
			}
			else
			{
				pos = start-1;
				do
				{
					pos = expression.indexOf("(", pos+1);
				}
				while (!expression.substring(pos, expression.indexOf(")", pos)+1).matches(".*\\(.*[0-9\\.]+[\\^\\*\\/\\+][0-9\\.]+.*\\).*"));
				end = calculate(pos, expression.indexOf(")", pos)+1);
			}
			if ((!expression.substring(start, end).matches("[-]*[a-zA-Z0-9\\.]+[ ]*")) && (!expression.substring(start, end).matches("\\(-[a-zA-Z0-9\\.]+\\)[ ]*")))
				end = calculate(start, end);
		}
		if ((!expression.toString().matches("[-]*[a-zA-Z0-9\\.]+[ ]*")) && (!expression.toString().matches("\\(-[a-zA-Z0-9\\.]+\\)[ ]*")))
			end = 			calculate(0, expression.length()-1);
		cleanUp();
		return end;
	}

	private static boolean isValid()
	{
		return (!((expression.toString().matches(".*[\\+\\-\\*\\/\\^][\\+\\-\\*\\/\\^].*")) || (expression.toString().matches(".*\\([\\+\\*\\/\\^]")) || (expression.toString().matches(".*[\\+\\-\\*\\/\\^]\\).*"))));
	}

	private static boolean isDigitOrPoint(char c)
	{
		return ((Character.isDigit(c)) || (c == '.') || (c == '(') || (c == ')') || (c == '-'));
	}

	private static boolean trimParenthesis(int start, int end)
	{
		if ((expression.charAt(start) == '(') && (expression.charAt(end-1) == ')') && (expression.indexOf(")", start) == end-1))
		{
			expression.deleteCharAt(end-1);
			expression.deleteCharAt(start);
			return true;
		}
		return false;
	}

	private static int findAndEvaluate(int start, int end, String operator)
	{
		for (amount = 0, pos = expression.indexOf(operator, start); ((pos > 0) && (pos < end)); pos = expression.indexOf(operator, start))
		{
			indexOf(start, end);
			if (expression.charAt(after-1) == ')')
			{
				expression.deleteCharAt(after-1);
				expression.deleteCharAt(pos+1);
				end-= 2;
				after-=2;
				amount+= 2;
			}
			if (expression.charAt(before) == '(')
			{
				expression.deleteCharAt(pos-1);
				expression.deleteCharAt(before);
				pos-=2;
				after-= 2;
				end-= 2;
				amount+= 2;
			}
			replacement = evaluate(Double.parseDouble(expression.substring(before, pos)), Double.parseDouble(expression.substring(pos+1, after)), operator.charAt(0))+"";
			amount+= end-start-replacement.length();
			end-= end-start-replacement.length();
			amount += end-start-replacement.length();
			expression.replace(before, after, replacement);
			if (expression.substring(end-2, end).equals(".0"))
			{
				expression.delete(end-2, end);
				end-= 2;
				amount += 2;
			}
			if (expression.charAt(before) == '-')
			{
				expression.insert(end, ')');
				expression.insert(before, '(');
				end+= 2;
				amount -= 2;
			}
		}
		return amount;
	}

	private static void indexOf(int start, int end)
	{
		for (before = pos-1; ((before > start) && (isDigitOrPoint(expression.charAt(before)))); before--);
		if ((expression.charAt(before) == '-') || (!isDigitOrPoint(expression.charAt(before)))) before++;
		for (after = pos+1; ((after < end) && (isDigitOrPoint(expression.charAt(after)))); after++);
	}

	private static void cleanUp()
	{
		if (expression.charAt(expression.length()-1) == ' ') expression.deleteCharAt(expression.length()-1);
		if (expression.charAt(0) == '(')
		{
			expression.deleteCharAt(0);
			expression.deleteCharAt(expression.length()-1);
		}
	}

	private static double evaluate(double num1, double num2, char operator)
	{
		switch (operator)
		{
			case '+':
				return num1+num2;
			case '-':
				return num1-num2;
			case '*':
				return num1*num2;
			case '/':
				return num1/num2;
			case '^':
				return Math.pow(num1, num2);
		}
		return 0;
	}

}
