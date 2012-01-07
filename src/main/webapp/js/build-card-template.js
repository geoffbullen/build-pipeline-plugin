<table class="build-card rounded">
	<tbody>
		<tr class="header">
			<td colspan="2">
				<div>
					<a title="{{title}}">{{title}}</a>
				</div>
			</td>
		</tr>
		<tr class="build-body">
			<td class="build-number">{{buildNumber}}</td>
			<td id="tooltip100" class="secondary-info">
				<div>{{date}}</div>
				<div>{{time}}</div>
				<div>{{duration}}</div>
			</td>
		</tr>
		<tr class="build-actions">
			<td colspan="2">
				<div class="status-bar"></div>
				<div class="icons">
					<a href="{{buildUrl}}/console">
						<img title="console" alt="console" src="{{rootUrl}}/images/16x16/terminal.png" />
					</a>
					<a href="{{buildUrl}}/changes">
						<img title="changes" alt="changes" src="{{rootUrl}}/images/16x16/notepad.png" />
					</a>
				</div>
			</td>
		</tr>
	</tbody>
</table>