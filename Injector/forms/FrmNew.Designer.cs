namespace KestrelClientInjector.forms
{
    partial class FrmNew
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
            Guna.UI2.WinForms.Suite.CustomizableEdges customizableEdges4 = new Guna.UI2.WinForms.Suite.CustomizableEdges();
            Guna.UI2.WinForms.Suite.CustomizableEdges customizableEdges5 = new Guna.UI2.WinForms.Suite.CustomizableEdges();
            Guna.UI2.WinForms.Suite.CustomizableEdges customizableEdges3 = new Guna.UI2.WinForms.Suite.CustomizableEdges();
            Guna.UI2.WinForms.Suite.CustomizableEdges customizableEdges1 = new Guna.UI2.WinForms.Suite.CustomizableEdges();
            Guna.UI2.WinForms.Suite.CustomizableEdges customizableEdges2 = new Guna.UI2.WinForms.Suite.CustomizableEdges();
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(FrmNew));
            userNavigationBar1 = new KestrelClientInjector.components.UserNavigationBar();
            Elipse = new Guna.UI2.WinForms.Guna2Elipse(components);
            btnInjector = new KestrelClientInjector.components.Button(components);
            lblProcessesTitle = new Label();
            lstMinecraftProcesses = new ListBox();
            btnRefreshProcesses = new KestrelClientInjector.components.Button(components);
            lblProcessCount = new Label();
            btnInject = new KestrelClientInjector.components.Button(components);
            btnUpdateClient = new KestrelClientInjector.components.Button(components);
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
            // Elipse
            // 
            Elipse.BorderRadius = 36;
            Elipse.TargetControl = this;
            // 
            // btnInjector
            // 
            btnInjector.Animated = true;
            btnInjector.BackColor = Color.Transparent;
            btnInjector.BorderRadius = 6;
            btnInjector.CustomizableEdges = customizableEdges4;
            btnInjector.DisabledState.BorderColor = Color.DarkGray;
            btnInjector.DisabledState.CustomBorderColor = Color.DarkGray;
            btnInjector.DisabledState.FillColor = Color.FromArgb(169, 169, 169);
            btnInjector.DisabledState.ForeColor = Color.FromArgb(141, 141, 141);
            btnInjector.FillColor = Color.FromArgb(255, 87, 34);
            btnInjector.Font = new Font("Trebuchet MS", 11F, FontStyle.Bold);
            btnInjector.ForeColor = Color.White;
            btnInjector.HoverState.FillColor = Color.FromArgb(231, 80, 34);
            btnInjector.IndicateFocus = true;
            btnInjector.Location = new Point(652, 49);
            btnInjector.Name = "btnInjector";
            btnInjector.PressedColor = Color.FromArgb(231, 80, 34);
            btnInjector.ShadowDecoration.CustomizableEdges = customizableEdges5;
            btnInjector.Size = new Size(123, 24);
            btnInjector.TabIndex = 1;
            btnInjector.TabStop = false;
            btnInjector.Text = "Old Injector";
            btnInjector.Click += btnInjector_Click;
            // 
            // lblProcessesTitle
            // 
            lblProcessesTitle.AutoSize = true;
            lblProcessesTitle.Font = new Font("Trebuchet MS", 12F, FontStyle.Bold);
            lblProcessesTitle.ForeColor = Color.White;
            lblProcessesTitle.Location = new Point(20, 51);
            lblProcessesTitle.Name = "lblProcessesTitle";
            lblProcessesTitle.Size = new Size(195, 22);
            lblProcessesTitle.TabIndex = 2;
            lblProcessesTitle.Text = "🎮 Minecraft Processes:";
            // 
            // lstMinecraftProcesses
            // 
            lstMinecraftProcesses.BackColor = Color.FromArgb(30, 30, 30);
            lstMinecraftProcesses.BorderStyle = BorderStyle.None;
            lstMinecraftProcesses.DrawMode = DrawMode.OwnerDrawVariable;
            lstMinecraftProcesses.Font = new Font("Trebuchet MS", 10F);
            lstMinecraftProcesses.ForeColor = Color.White;
            lstMinecraftProcesses.FormattingEnabled = true;
            lstMinecraftProcesses.ItemHeight = 60;
            lstMinecraftProcesses.Location = new Point(20, 90);
            lstMinecraftProcesses.Name = "lstMinecraftProcesses";
            lstMinecraftProcesses.Size = new Size(755, 300);
            lstMinecraftProcesses.TabIndex = 3;
            lstMinecraftProcesses.DrawItem += lstMinecraftProcesses_DrawItem;
            lstMinecraftProcesses.MeasureItem += lstMinecraftProcesses_MeasureItem;
            lstMinecraftProcesses.SelectedIndexChanged += lstMinecraftProcesses_SelectedIndexChanged;
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
            btnRefreshProcesses.FillColor = Color.FromArgb(76, 175, 80);
            btnRefreshProcesses.Font = new Font("Trebuchet MS", 15F, FontStyle.Bold);
            btnRefreshProcesses.ForeColor = Color.White;
            btnRefreshProcesses.HoverState.FillColor = Color.FromArgb(69, 160, 73);
            btnRefreshProcesses.IndicateFocus = true;
            btnRefreshProcesses.Location = new Point(599, 403);
            btnRefreshProcesses.Name = "btnRefreshProcesses";
            btnRefreshProcesses.PressedColor = Color.FromArgb(69, 160, 73);
            btnRefreshProcesses.ShadowDecoration.CustomizableEdges = customizableEdges3;
            btnRefreshProcesses.Size = new Size(47, 33);
            btnRefreshProcesses.TabIndex = 4;
            btnRefreshProcesses.TabStop = false;
            btnRefreshProcesses.Text = "🔄 Refresh";
            btnRefreshProcesses.Click += btnRefreshProcesses_Click;
            // 
            // lblProcessCount
            // 
            lblProcessCount.AutoSize = true;
            lblProcessCount.Font = new Font("Trebuchet MS", 9F);
            lblProcessCount.ForeColor = Color.FromArgb(175, 175, 175);
            lblProcessCount.Location = new Point(20, 418);
            lblProcessCount.Name = "lblProcessCount";
            lblProcessCount.Size = new Size(118, 18);
            lblProcessCount.TabIndex = 5;
            lblProcessCount.Text = "No processes found";
            // 
            // btnInject
            // 
            btnInject.Animated = true;
            btnInject.BackColor = Color.Transparent;
            btnInject.BorderRadius = 6;
            btnInject.CustomizableEdges = customizableEdges1;
            btnInject.DisabledState.BorderColor = Color.DarkGray;
            btnInject.DisabledState.CustomBorderColor = Color.DarkGray;
            btnInject.DisabledState.FillColor = Color.FromArgb(169, 169, 169);
            btnInject.DisabledState.ForeColor = Color.FromArgb(141, 141, 141);
            btnInject.FillColor = Color.FromArgb(255, 87, 34);
            btnInject.Font = new Font("Trebuchet MS", 11F, FontStyle.Bold);
            btnInject.ForeColor = Color.White;
            btnInject.HoverState.FillColor = Color.FromArgb(231, 80, 34);
            btnInject.IndicateFocus = true;
            btnInject.Location = new Point(652, 403);
            btnInject.Name = "btnInject";
            btnInject.PressedColor = Color.FromArgb(231, 80, 34);
            btnInject.ShadowDecoration.CustomizableEdges = customizableEdges2;
            btnInject.Size = new Size(123, 33);
            btnInject.TabIndex = 6;
            btnInject.TabStop = false;
            btnInject.Text = "Inject";
            btnInject.Click += btnInject_Click;
            // 
            // btnUpdateClient
            // 
            btnUpdateClient.Animated = true;
            btnUpdateClient.BackColor = Color.Transparent;
            btnUpdateClient.BorderRadius = 6;
            btnUpdateClient.CustomizableEdges = customizableEdges2;
            btnUpdateClient.DisabledState.BorderColor = Color.DarkGray;
            btnUpdateClient.DisabledState.CustomBorderColor = Color.DarkGray;
            btnUpdateClient.DisabledState.FillColor = Color.FromArgb(169, 169, 169);
            btnUpdateClient.DisabledState.ForeColor = Color.FromArgb(141, 141, 141);
            btnUpdateClient.FillColor = Color.FromArgb(33, 150, 243);
            btnUpdateClient.Font = new Font("Trebuchet MS", 13F, FontStyle.Bold);
            btnUpdateClient.ForeColor = Color.White;
            btnUpdateClient.HoverState.FillColor = Color.FromArgb(30, 136, 229);
            btnUpdateClient.IndicateFocus = true;
            btnUpdateClient.Location = new Point(473, 403);
            btnUpdateClient.Name = "btnUpdateClient";
            btnUpdateClient.PressedColor = Color.FromArgb(30, 136, 229);
            btnUpdateClient.ShadowDecoration.CustomizableEdges = customizableEdges2;
            btnUpdateClient.Size = new Size(120, 33);
            btnUpdateClient.TabIndex = 7;
            btnUpdateClient.TabStop = false;
            btnUpdateClient.Text = "🔄 Update Client";
            btnUpdateClient.Click += btnUpdateClient_Click;
            // 
            // FrmNew
            // 
            AutoScaleDimensions = new SizeF(7F, 15F);
            AutoScaleMode = AutoScaleMode.Font;
            BackColor = Color.FromArgb(15, 15, 15);
            ClientSize = new Size(800, 450);
            Controls.Add(btnInject);
            Controls.Add(btnUpdateClient);
            Controls.Add(lblProcessCount);
            Controls.Add(btnRefreshProcesses);
            Controls.Add(lstMinecraftProcesses);
            Controls.Add(lblProcessesTitle);
            Controls.Add(btnInjector);
            Controls.Add(userNavigationBar1);
            ForeColor = Color.White;
            FormBorderStyle = FormBorderStyle.None;
            Icon = (Icon)resources.GetObject("$this.Icon");
            Name = "FrmNew";
            StartPosition = FormStartPosition.CenterScreen;
            Text = "KestrelClient";
            TopMost = true;
            Load += FrmNew_Load;
            ResumeLayout(false);
            PerformLayout();
        }

        #endregion

        private components.UserNavigationBar userNavigationBar1;
        private Guna.UI2.WinForms.Guna2Elipse Elipse;
        private components.Button btnInjector;
        private Label lblProcessesTitle;
        private ListBox lstMinecraftProcesses;
        private components.Button btnRefreshProcesses;
        private Label lblProcessCount;
        private components.Button btnInject;
        private components.Button btnUpdateClient;
    }
}