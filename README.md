SignalMonitor
=============

Application to send all OpenXC signals only when a monitored signal crosses a designated threshold.

Rough Project Plan
==================

The steps and milestones I see as being somthing like this:

1) get code capable of sending fixed JSON 'blob' to URL via POST.
2) make modified copy of MyFirstApp read 'Watchers.txt' from SD card
3) modify same to parse JSON list of monitored signals
4) modify same to synchronously/asyncrhonously (haven't made up my mind yet) monitor signals and detect crossing thresholds
5) modify same to send the JSON object announcing the change, based on 1.
6) finally, modify same to send 

Q: what do we do if we have multiple signals that crossed the
threshold? Probably the worst option would be to keep the agreed
format, sending only one that changed each time.




