﻿using System.ComponentModel;

namespace KestrelClientInjector.components
{
    public partial class TextBox : Guna.UI2.WinForms.Guna2TextBox
    {
        public TextBox()
        {
            InitializeComponent();
        }

        public TextBox(IContainer container)
        {
            container.Add(this);

            InitializeComponent();
        }
    }
}