﻿using System.ComponentModel;

namespace KestrelClientInjector.components
{
    public partial class CheckBox : Guna.UI2.WinForms.Guna2CheckBox
    {
        public CheckBox()
        {
            InitializeComponent();
        }

        public CheckBox(IContainer container)
        {
            container.Add(this);

            InitializeComponent();
        }
    }
}
