<h1>FRDL Overview</h1>

<b>F</b>light <b>R</b>ecorder <b>D</b>own<b>L</b>oader



&lt;H2&gt;

FRDL is designed to be as idiot proof as possible....

&lt;/H2&gt;



FRDL is designed for Microlight and Paramotor competition organizers,
and for team leaders or pilots to all quickly reliably and painlessly
extract track data relavent to competition tasks from a variety
of different GPS flight loggers, and then convert it into the FAI
CIMA specified format for detailed examination in a flight analysis program.
<p>
FRDL backs up all log files it finds on a logger and instantly displays<br>
a simple outline and altitude profile of the track it has<br>
written to the output .igc file.  This means the downloading can<br>
be done the moment the pilot lands from a championship task, and in the<br>
mutual knowledge of what has been saved, the logger can confidently be<br>
immediately returned to the pilot.<br>
<p>
FRDL maintains all the information it needs about the championship,<br>
championship tasks, and the loggers in use in a <b>championship file</b>
(<b>.frdc).   Once a championship file is open, FRDL is ready for use.</b>


<p>
FRDL operates in two different <b>modes</b>:<br>
<ul>
<blockquote><li><b>Full mode</b> is for normal everyday use and for use by the<br>
championship organization.</li>
<li><b>Download only mode</b> Designed for team leaders and pilots.  In<br>
this mode the settings (task windows, local time offset Etc.) are copied<br>
from the logger to their instance of FRDL so they can extract tracks from their<br>
loggers using the exact same settings as the organizer has used.  This<br>
means they will be able to do their own flight analysis on the same<br>
basis as the championship organization.<br>
</li>
</ul>
For further information, see the <b>Modes of operation</b> help item.</blockquote>


<h2>Basic sequence of events</h2>

FRDL keeps a <b>constant watch</b> on USB ports.  If a logger is<br>
connected, then FRDL  attempts to identify it from a special file<br>
on the logger (logger.frdl) which uniquely identifies it and which<br>
was written to it the first time it was connected to FRDL.<br>
<br>
<p>
Once a logger is identified, FRDL immediately <b>backs up</b> its<br>
entire contents to the host computer.<br>
<br>
<p>
FRDL then attempts to extract from this data all GPS fix information which<br>
occurred within a pre-set <b>Task window</b>.<br>
<br>
<p>
This is saved to a <b>CIMA specification .igc file</b> which<br>
is used by other software (eg MicroFlap) to analyse the flight data.  This is<br>
exactly the same data format as output by downloaders of other loggers such<br>
as the MLR.<br>
<br>
<p>
A <b>basic outline</b> of the track, its altitude profile and some statistics<br>
are displayed on screen as a 'quick and dirty' indication of what has<br>
been saved. It is NOT intended to constitute any kind of definitive flight<br>
analysis which should be done in a dedicated flight analysis<br>
program (eg MicroFlap), but it is probably good enough for both the<br>
organizer and the pilot to clearly understand what was recorded by the logger so<br>
it can be immediately returned to him.  It may also indicate poor reception which<br>
can be improved by the pilot placing the logger in a better place in his aircraft<br>
next time, and alert the organizer to important penalties such as outlandings which<br>
merit further detailed investigation.<br>
<br>
<p>

Assuming a <b>USB-2</b> connection, a <b>good quality USB cable</b> and the<br>
data is being saved on the <b>local computer</b>, the whole process should<br>
take no more than <b>a few seconds</b>.  If the data is being saved to a<br>
network location then the limiting factor will be the speed of the network<br>
rather than anything to do with FRDL.<br>
<br>
<h3>
FRDL is in a constant state of improvement so please ALWAYS check<br>
<b>www.flymicro.com/frdl</b> for the latest version.</h3>