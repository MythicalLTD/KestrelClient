namespace KestrelClientInjector.forms
{
    partial class FrmMain
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            components = new System.ComponentModel.Container();
            Guna.UI2.WinForms.Suite.CustomizableEdges customizableEdges1 = new Guna.UI2.WinForms.Suite.CustomizableEdges();
            Guna.UI2.WinForms.Suite.CustomizableEdges customizableEdges2 = new Guna.UI2.WinForms.Suite.CustomizableEdges();
            Guna.UI2.WinForms.Suite.CustomizableEdges customizableEdges3 = new Guna.UI2.WinForms.Suite.CustomizableEdges();
            Guna.UI2.WinForms.Suite.CustomizableEdges customizableEdges4 = new Guna.UI2.WinForms.Suite.CustomizableEdges();
            Guna.UI2.WinForms.Suite.CustomizableEdges customizableEdges5 = new Guna.UI2.WinForms.Suite.CustomizableEdges();
            Guna.UI2.WinForms.Suite.CustomizableEdges customizableEdges6 = new Guna.UI2.WinForms.Suite.CustomizableEdges();
            Guna.UI2.WinForms.Suite.CustomizableEdges customizableEdges7 = new Guna.UI2.WinForms.Suite.CustomizableEdges();
            Guna.UI2.WinForms.Suite.CustomizableEdges customizableEdges8 = new Guna.UI2.WinForms.Suite.CustomizableEdges();
            Guna.UI2.WinForms.Suite.CustomizableEdges customizableEdges9 = new Guna.UI2.WinForms.Suite.CustomizableEdges();
            Guna.UI2.WinForms.Suite.CustomizableEdges customizableEdges10 = new Guna.UI2.WinForms.Suite.CustomizableEdges();
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(FrmMain));
            userNavigationBar1 = new KestrelClientInjector.components.UserNavigationBar();
            btnNext = new KestrelClientInjector.components.Button(components);
            lbPrograms = new ListBox();
            txtSearch = new KestrelClientInjector.components.TextBox(components);
            lblTitle = new Label();
            lblInstructions = new Label();
            btnRefreshProcesses = new KestrelClientInjector.components.Button(components);
            cbShowAllProc = new KestrelClientInjector.components.CheckBox(components);
            cbIgnoreSecurity = new KestrelClientInjector.components.CheckBox(components);
            txtDLLPath = new KestrelClientInjector.components.TextBox(components);
            btnPickDLL = new KestrelClientInjector.components.Button(components);
            label1 = new KestrelClientInjector.components.Label(components);
            pbProgress = new KestrelClientInjector.components.ProgressBar(components);
            guna2Elipse1 = new Guna.UI2.WinForms.Guna2Elipse(components);
            SuspendLayout();
            // 
            // userNavigationBar1
            // 
            userNavigationBar1.BackColor = Color.FromArgb(25, 25, 25);
            userNavigationBar1.Dock = DockStyle.Top;
            userNavigationBar1.Location = new Point(0, 0);
            userNavigationBar1.Name = "userNavigationBar1";
            userNavigationBar1.Size = new Size(800, 37);
            userNavigationBar1.TabIndex = 0;
            // 
            // btnNext
            // 
            btnNext.Animated = true;
            btnNext.BackColor = Color.Transparent;
            btnNext.BorderRadius = 6;
            btnNext.CustomizableEdges = customizableEdges1;
            btnNext.DisabledState.BorderColor = Color.DarkGray;
            btnNext.DisabledState.CustomBorderColor = Color.DarkGray;
            btnNext.DisabledState.FillColor = Color.FromArgb(169, 169, 169);
            btnNext.DisabledState.ForeColor = Color.FromArgb(141, 141, 141);
            btnNext.Enabled = false;
            btnNext.FillColor = Color.FromArgb(255, 87, 34);
            btnNext.Font = new Font("Trebuchet MS", 11F, FontStyle.Bold);
            btnNext.ForeColor = Color.White;
            btnNext.HoverState.FillColor = Color.FromArgb(231, 80, 34);
            btnNext.IndicateFocus = true;
            btnNext.Location = new Point(608, 387);
            btnNext.Name = "btnNext";
            btnNext.PressedColor = Color.FromArgb(231, 80, 34);
            btnNext.ShadowDecoration.CustomizableEdges = customizableEdges2;
            btnNext.Size = new Size(180, 45);
            btnNext.TabIndex = 1;
            btnNext.TabStop = false;
            btnNext.Text = "Inject";
            btnNext.Click += btnNext_Click;
            // 
            // lbPrograms
            // 
            lbPrograms.BackColor = Color.FromArgb(25, 25, 25);
            lbPrograms.BorderStyle = BorderStyle.None;
            lbPrograms.Font = new Font("Trebuchet MS", 10F);
            lbPrograms.ForeColor = Color.White;
            lbPrograms.FormattingEnabled = true;
            lbPrograms.Location = new Point(12, 135);
            lbPrograms.Name = "lbPrograms";
            lbPrograms.Size = new Size(776, 198);
            lbPrograms.TabIndex = 2;
            lbPrograms.SelectedIndexChanged += lbPrograms_SelectedIndexChanged;
            // 
            // txtSearch
            // 
            txtSearch.Animated = true;
            txtSearch.BackColor = Color.Transparent;
            txtSearch.BorderColor = Color.FromArgb(75, 75, 75);
            txtSearch.BorderRadius = 6;
            txtSearch.CustomizableEdges = customizableEdges3;
            txtSearch.DefaultText = "";
            txtSearch.DisabledState.BorderColor = Color.FromArgb(208, 208, 208);
            txtSearch.DisabledState.FillColor = Color.FromArgb(226, 226, 226);
            txtSearch.DisabledState.ForeColor = Color.FromArgb(138, 138, 138);
            txtSearch.DisabledState.PlaceholderForeColor = Color.FromArgb(138, 138, 138);
            txtSearch.FillColor = Color.FromArgb(35, 35, 35);
            txtSearch.FocusedState.BorderColor = Color.FromArgb(255, 87, 34);
            txtSearch.Font = new Font("Trebuchet MS", 10F);
            txtSearch.ForeColor = Color.White;
            txtSearch.HoverState.BorderColor = Color.FromArgb(255, 87, 34);
            txtSearch.Location = new Point(12, 95);
            txtSearch.Margin = new Padding(4, 3, 4, 3);
            txtSearch.Name = "txtSearch";
            txtSearch.PlaceholderForeColor = Color.FromArgb(125, 125, 125);
            txtSearch.PlaceholderText = "🔍 Search for games and applications...";
            txtSearch.SelectedText = "";
            txtSearch.ShadowDecoration.CustomizableEdges = customizableEdges4;
            txtSearch.Size = new Size(649, 30);
            txtSearch.TabIndex = 3;
            txtSearch.TextChanged += txtSearch_TextChanged;
            // 
            // lblTitle
            // 
            lblTitle.AutoSize = true;
            lblTitle.Font = new Font("Trebuchet MS", 14F, FontStyle.Bold);
            lblTitle.ForeColor = Color.FromArgb(255, 87, 34);
            lblTitle.Location = new Point(12, 45);
            lblTitle.Name = "lblTitle";
            lblTitle.Size = new Size(202, 24);
            lblTitle.TabIndex = 4;
            lblTitle.Text = "Select Target Process";
            // 
            // lblInstructions
            // 
            lblInstructions.AutoSize = true;
            lblInstructions.Font = new Font("Trebuchet MS", 9F);
            lblInstructions.ForeColor = Color.FromArgb(175, 175, 175);
            lblInstructions.Location = new Point(12, 69);
            lblInstructions.Name = "lblInstructions";
            lblInstructions.Size = new Size(523, 18);
            lblInstructions.TabIndex = 5;
            lblInstructions.Text = "🛡️ Critical Windows processes are filtered out for your safety. Select a game or application.";
            // 
            // btnRefreshProcesses
            // 
            btnRefreshProcesses.Animated = true;
            btnRefreshProcesses.BackColor = Color.Transparent;
            btnRefreshProcesses.BorderRadius = 6;
            btnRefreshProcesses.CustomizableEdges = customizableEdges3;
            btnRefreshProcesses.DisabledState.BorderColor = Color.DarkGray;
            btnRefreshProcesses.DisabledState.CustomBorderColor = Color.DarkGray;
            btnRefreshProcesses.DisabledState.FillColor = Color.FromArgb(169, 169, 169);
            btnRefreshProcesses.DisabledState.ForeColor = Color.FromArgb(141, 141, 141);
            btnRefreshProcesses.FillColor = Color.FromArgb(67, 181, 129);
            btnRefreshProcesses.Font = new Font("Trebuchet MS", 10F, FontStyle.Bold);
            btnRefreshProcesses.ForeColor = Color.White;
            btnRefreshProcesses.HoverState.FillColor = Color.FromArgb(60, 160, 115);
            btnRefreshProcesses.IndicateFocus = true;
            btnRefreshProcesses.Location = new Point(668, 95);
            btnRefreshProcesses.Name = "btnRefreshProcesses";
            btnRefreshProcesses.PressedColor = Color.FromArgb(60, 160, 115);
            btnRefreshProcesses.ShadowDecoration.CustomizableEdges = customizableEdges4;
            btnRefreshProcesses.Size = new Size(120, 30);
            btnRefreshProcesses.TabIndex = 6;
            btnRefreshProcesses.TabStop = false;
            btnRefreshProcesses.Text = "🔄 Refresh";
            btnRefreshProcesses.Click += btnRefreshProcesses_Click;
            // 
            // cbIgnoreSecurity
            // 
            cbIgnoreSecurity.Animated = true;
            cbIgnoreSecurity.AutoSize = true;
            cbIgnoreSecurity.BackColor = Color.Transparent;
            cbIgnoreSecurity.CheckedState.BorderColor = Color.FromArgb(255, 87, 34);
            cbIgnoreSecurity.CheckedState.BorderRadius = 0;
            cbIgnoreSecurity.CheckedState.BorderThickness = 0;
            cbIgnoreSecurity.CheckedState.FillColor = Color.FromArgb(255, 87, 34);
            cbIgnoreSecurity.Font = new Font("Trebuchet MS", 9F, FontStyle.Bold);
            cbIgnoreSecurity.ForeColor = Color.FromArgb(255, 193, 7);
            cbIgnoreSecurity.Location = new Point(13, 443);
            cbIgnoreSecurity.Name = "cbIgnoreSecurity";
            cbIgnoreSecurity.Size = new Size(200, 22);
            cbIgnoreSecurity.TabIndex = 12;
            cbIgnoreSecurity.Text = "⚠️ Ignore Security Flags";
            cbIgnoreSecurity.UncheckedState.BorderColor = Color.FromArgb(38, 38, 38);
            cbIgnoreSecurity.UncheckedState.BorderRadius = 0;
            cbIgnoreSecurity.UncheckedState.BorderThickness = 0;
            cbIgnoreSecurity.UncheckedState.FillColor = Color.FromArgb(38, 38, 38);
            cbIgnoreSecurity.UseVisualStyleBackColor = false;
            // 
            // cbShowAllProc
            // 
            cbShowAllProc.Animated = true;
            cbShowAllProc.AutoSize = true;
            cbShowAllProc.BackColor = Color.Transparent;
            cbShowAllProc.CheckedState.BorderColor = Color.FromArgb(231, 80, 34);
            cbShowAllProc.CheckedState.BorderRadius = 0;
            cbShowAllProc.CheckedState.BorderThickness = 0;
            cbShowAllProc.CheckedState.FillColor = Color.FromArgb(231, 80, 34);
            cbShowAllProc.Font = new Font("Trebuchet MS", 9F, FontStyle.Bold);
            cbShowAllProc.ForeColor = Color.White;
            cbShowAllProc.Location = new Point(608, 470);
            cbShowAllProc.Name = "cbShowAllProc";
            cbShowAllProc.Size = new Size(177, 22);
            cbShowAllProc.TabIndex = 7;
            cbShowAllProc.Text = "Show all unsafe processes!";
            cbShowAllProc.UncheckedState.BorderColor = Color.FromArgb(38, 38, 38);
            cbShowAllProc.UncheckedState.BorderRadius = 0;
            cbShowAllProc.UncheckedState.BorderThickness = 0;
            cbShowAllProc.UncheckedState.FillColor = Color.FromArgb(38, 38, 38);
            cbShowAllProc.UseVisualStyleBackColor = false;
            // 
            // txtDLLPath
            // 
            txtDLLPath.Animated = true;
            txtDLLPath.BackColor = Color.Transparent;
            txtDLLPath.BorderColor = Color.FromArgb(75, 75, 75);
            txtDLLPath.BorderRadius = 6;
            txtDLLPath.CustomizableEdges = customizableEdges5;
            txtDLLPath.DefaultText = "";
            txtDLLPath.DisabledState.BorderColor = Color.FromArgb(208, 208, 208);
            txtDLLPath.DisabledState.FillColor = Color.FromArgb(226, 226, 226);
            txtDLLPath.DisabledState.ForeColor = Color.FromArgb(138, 138, 138);
            txtDLLPath.DisabledState.PlaceholderForeColor = Color.FromArgb(138, 138, 138);
            txtDLLPath.FillColor = Color.FromArgb(35, 35, 35);
            txtDLLPath.FocusedState.BorderColor = Color.FromArgb(255, 87, 34);
            txtDLLPath.Font = new Font("Trebuchet MS", 10F);
            txtDLLPath.ForeColor = Color.White;
            txtDLLPath.HoverState.BorderColor = Color.FromArgb(255, 87, 34);
            txtDLLPath.Location = new Point(13, 387);
            txtDLLPath.Margin = new Padding(4, 3, 4, 3);
            txtDLLPath.Name = "txtDLLPath";
            txtDLLPath.PlaceholderForeColor = Color.FromArgb(125, 125, 125);
            txtDLLPath.PlaceholderText = "Select DLL file to inject...";
            txtDLLPath.SelectedText = "";
            txtDLLPath.ShadowDecoration.CustomizableEdges = customizableEdges6;
            txtDLLPath.Size = new Size(462, 47);
            txtDLLPath.TabIndex = 8;
            // 
            // btnPickDLL
            // 
            btnPickDLL.Animated = true;
            btnPickDLL.BackColor = Color.Transparent;
            btnPickDLL.BorderRadius = 6;
            btnPickDLL.CustomizableEdges = customizableEdges7;
            btnPickDLL.DisabledState.BorderColor = Color.DarkGray;
            btnPickDLL.DisabledState.CustomBorderColor = Color.DarkGray;
            btnPickDLL.DisabledState.FillColor = Color.FromArgb(169, 169, 169);
            btnPickDLL.DisabledState.ForeColor = Color.FromArgb(141, 141, 141);
            btnPickDLL.FillColor = Color.FromArgb(0, 192, 192);
            btnPickDLL.Font = new Font("Trebuchet MS", 10F, FontStyle.Bold);
            btnPickDLL.ForeColor = Color.White;
            btnPickDLL.HoverState.FillColor = Color.FromArgb(60, 160, 115);
            btnPickDLL.IndicateFocus = true;
            btnPickDLL.Location = new Point(482, 387);
            btnPickDLL.Name = "btnPickDLL";
            btnPickDLL.PressedColor = Color.Teal;
            btnPickDLL.ShadowDecoration.CustomizableEdges = customizableEdges8;
            btnPickDLL.Size = new Size(120, 45);
            btnPickDLL.TabIndex = 9;
            btnPickDLL.TabStop = false;
            btnPickDLL.Text = "🔄 Pick DLL";
            // 
            // label1
            // 
            label1.AutoSize = false;
            label1.BackColor = Color.Transparent;
            label1.Font = new Font("Trebuchet MS", 12F);
            label1.ForeColor = Color.White;
            label1.Location = new Point(13, 470);
            label1.Name = "label1";
            label1.Size = new Size(589, 30);
            label1.TabIndex = 10;
            label1.Text = "label1";
            // 
            // pbProgress
            // 
            pbProgress.BorderColor = Color.Transparent;
            pbProgress.BorderRadius = 6;
            pbProgress.CustomizableEdges = customizableEdges9;
            pbProgress.FillColor = Color.FromArgb(38, 38, 38);
            pbProgress.Location = new Point(13, 345);
            pbProgress.Name = "pbProgress";
            pbProgress.ProgressColor = Color.FromArgb(231, 80, 34);
            pbProgress.ProgressColor2 = Color.FromArgb(255, 87, 34);
            pbProgress.ShadowDecoration.CustomizableEdges = customizableEdges10;
            pbProgress.Size = new Size(775, 30);
            pbProgress.Style = ProgressBarStyle.Continuous;
            pbProgress.TabIndex = 11;
            pbProgress.Text = "progressBar1";
            pbProgress.TextRenderingHint = System.Drawing.Text.TextRenderingHint.SystemDefault;
            // 
            // guna2Elipse1
            // 
            guna2Elipse1.BorderRadius = 36;
            guna2Elipse1.TargetControl = this;
            // 
            // FrmMain
            // 
            AutoScaleDimensions = new SizeF(7F, 15F);
            AutoScaleMode = AutoScaleMode.Font;
            BackColor = Color.FromArgb(15, 15, 15);
            ClientSize = new Size(800, 477);
            Controls.Add(pbProgress);
            Controls.Add(label1);
            Controls.Add(btnPickDLL);
            Controls.Add(txtDLLPath);
            Controls.Add(cbIgnoreSecurity);
            Controls.Add(cbShowAllProc);
            Controls.Add(btnRefreshProcesses);
            Controls.Add(lblInstructions);
            Controls.Add(lblTitle);
            Controls.Add(txtSearch);
            Controls.Add(lbPrograms);
            Controls.Add(btnNext);
            Controls.Add(userNavigationBar1);
            FormBorderStyle = FormBorderStyle.None;
            Icon = (Icon)resources.GetObject("$this.Icon");
            Name = "FrmMain";
            StartPosition = FormStartPosition.CenterScreen;
            Text = "Kestrel Client";
            Load += FrmMain_Load;
            ResumeLayout(false);
            PerformLayout();
        }

        #endregion

        private components.UserNavigationBar userNavigationBar1;
        private components.Button btnNext;
        private ListBox lbPrograms;
        private components.TextBox txtSearch;
        private Label lblTitle;
        private Label lblInstructions;
        private components.Button btnRefreshProcesses;
        private components.CheckBox cbShowAllProc;
        private components.CheckBox cbIgnoreSecurity;
        private components.TextBox txtDLLPath;
        private components.Button btnPickDLL;
        private components.Label label1;
        private components.ProgressBar pbProgress;
        private Guna.UI2.WinForms.Guna2Elipse guna2Elipse1;
    }
}