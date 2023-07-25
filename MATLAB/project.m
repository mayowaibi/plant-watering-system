clear all; close all;
a = arduino('/dev/cu.usbserial-0001', 'uno');

% typically assumed voltage values for different moisture levels
dryValue = 3.7;
semiSaturatedValue = 3.0;
saturatedValue = 2.8;

% graph specifications
figure(1);
h = animatedline;
ax = gca;
ax.YGrid = 'on';
ax.YLim = [0 1];
title('Moisture vs Time (LIVE)');
xlabel('Time (hh:mm:ss)');
ylabel('Moisture Level');
stop = false;
startTime = datetime('now');

while ~stop
    % read current voltage value
    voltage = readVoltage(a,'A1');
    if voltage > semiSaturatedValue % if dry, then water
        writeDigitalPin(a, 'D2', 1); pause(0.2); writeDigitalPin(a, 'D2', 0);
        disp("Soil is dry!");
    elseif voltage <= semiSaturatedValue  && voltage > saturatedValue % if wet, but not too wet, then water
        writeDigitalPin(a, 'D2', 1); pause(0.1); writeDigitalPin(a, 'D2', 0);
        disp("Just a little bit of water.");
    else % if saturated, do not water
        disp("Soil is saturated.");
    end
    moistureLevel = moistureConverter(voltage);
    % get current time
    t = datetime('now') - startTime;
    % add points to animation and update axes
    addpoints(h,datenum(t),moistureLevel)
    ax.XLim = datenum([t-seconds(15) t]);
    datetick('x','keeplimits')
    drawnow
    % stop if button is pressed
    stop = readDigitalPin(a,'D6');
end

% mathematical model to convert voltage to moisure level (on a scale
% of 0 (very dry) to 1 (very wet).
function moisture = moistureConverter(x)
    dryValue = 3.7;
    saturatedValue = 2.8;
    gradient = -1/(dryValue - (saturatedValue - 0.3));
    intercept = 1 - (saturatedValue - 0.3) * gradient;
    moisture = gradient * x + intercept; % from y = mx + c
end