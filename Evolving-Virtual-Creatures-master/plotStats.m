x=load('statsOut.txt');
tau=figure;
set(gca,'fontsize',14);
plot(x(:,1),x(:,2:3));
legend('Best','Average','Location','Best');
title('Evolving Virtual Creatures');
xlabel('Time (s)');
%ylabel('Fitness');
set(tau,'Units','Inches');
pos=get(tau,'Position');
set(tau,'PaperPositionMode','Auto','PaperUnits','Inches','PaperSize',[pos(3), pos(4)])
print(tau,'plotStats.pdf','-dpdf','-r600')

